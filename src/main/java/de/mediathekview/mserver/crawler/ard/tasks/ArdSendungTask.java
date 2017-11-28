package de.mediathekview.mserver.crawler.ard.tasks;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.ard.ArdSendungBasicInformation;
import de.mediathekview.mserver.crawler.ard.json.ArdBasicInfoDTO;
import de.mediathekview.mserver.crawler.ard.json.ArdBasicInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDTO;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import mServer.crawler.CrawlerTool;
import mServer.tool.MserverDatumZeit;

/**
 * Recursively crawls the ARD Sendungsfolge page.
 */
public class ArdSendungTask extends AbstractDocumentTask<Film, ArdSendungBasicInformation> {
  private static final String NO_VALID_URL = "Can't find a valid URL for \"%s\" from \"%s\".";
  private static final long serialVersionUID = -1528093537733110822L;
  private static final Logger LOG = LogManager.getLogger(ArdSendungTask.class);
  private static final String REGEX_PATTERN_DOCUMENT_ID = "(?<=&documentId=)\\d+";
  private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN =
      "Something terrible happened while convert \"%s\" to a film.";
  private static final String URL_BASE_PATTERN_BASIC_INFO =
      "http://www.ardmediathek.de/play/sola/%s";
  private static final String URL_BASE_PATTERN_VIDEO_INFO =
      "http://www.ardmediathek.de/play/media/%s?devicetype=pc&features=flash";
  private static final String SELECTOR_CLIP_INFO = "div.modClipinfo p.subtitle";
  private static final String SPLITTERATOR_CLIP_INFO = "\\s\\|\\s";
  private static final String REGEX_PATTERN_NUMBERS = "\\d+";

  public ArdSendungTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<ArdSendungBasicInformation> aArdSendungBasicInformation) {
    super(aCrawler, aArdSendungBasicInformation);
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
      throws MalformedURLException {
    for (final Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
    }
  }

  private String buildBasicInfoUrl(final String aDocumentId) {
    return String.format(URL_BASE_PATTERN_BASIC_INFO, aDocumentId);
  }

  private String buildVideoInfoUrl(final String aDocumentId) {
    return String.format(URL_BASE_PATTERN_VIDEO_INFO, aDocumentId);
  }

  private Gson createGson() {
    return new GsonBuilder()
        .registerTypeAdapter(ArdBasicInfoDTO.class, new ArdBasicInfoJsonDeserializer())
        .registerTypeAdapter(ArdVideoInfoDTO.class, new ArdVideoInfoJsonDeserializer(crawler))
        .create();
  }

  private String gatherDatum(final String[] aClipInfo) {
    return aClipInfo[0];
  }

  private int gatherDauerInMinutes(final String[] aClipInfo) {
    final Matcher numberMatcher = Pattern.compile(REGEX_PATTERN_NUMBERS).matcher(aClipInfo[1]);
    numberMatcher.find();
    return Integer.parseInt(numberMatcher.group());
  }

  private String[] getClipInfo(final Document aDocument) {
    return aDocument.select(SELECTOR_CLIP_INFO).text().split(SPLITTERATOR_CLIP_INFO);
  }

  private String getDocumentIdFromUrl(final String aUrl) {
    final Matcher documentIdRegexMatcher = Pattern.compile(REGEX_PATTERN_DOCUMENT_ID).matcher(aUrl);
    return documentIdRegexMatcher.find() ? documentIdRegexMatcher.group() : "";
  }

  private Sender getSenderFromName(final String aSenderName) {
    final Optional<Sender> foundSender = Sender.getSenderByName(aSenderName);
    return foundSender.isPresent() ? foundSender.get() : Sender.ARD;
  }

  @Override
  protected ArdSendungTask createNewOwnInstance(
      final ConcurrentLinkedQueue<ArdSendungBasicInformation> aURLsToCrawl) {
    return new ArdSendungTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final ArdSendungBasicInformation aUrlDTO,
      final Document aDocument) {
    try {
      final String[] clipInfo = getClipInfo(aDocument);
      final String datumAsText = gatherDatum(clipInfo);
      final int dauerInMinutes = gatherDauerInMinutes(clipInfo);

      final String sendezeitAsText = aUrlDTO.getSendezeitAsText();
      final String documentId = getDocumentIdFromUrl(aUrlDTO.getUrl());

      final Gson gson = createGson();
      final ArdBasicInfoDTO basicInfo =
          gson.fromJson(new InputStreamReader(new URL(buildBasicInfoUrl(documentId)).openStream()),
              ArdBasicInfoDTO.class);

      final ArdVideoInfoDTO videoInfo =
          gson.fromJson(new InputStreamReader(new URL(buildVideoInfoUrl(documentId)).openStream()),
              ArdVideoInfoDTO.class);

      final Sender sender = getSenderFromName(basicInfo.getSenderName());
      if (videoInfo.getVideoUrls().isEmpty()) {
        LOG.error(String.format(NO_VALID_URL, aUrlDTO.getUrl(), crawler.getSender().getName()));
        crawler.printErrorMessage();
        crawler.incrementAndGetErrorCount();
      } else {
        final Film newFilm = new Film(UUID.randomUUID(), sender, basicInfo.getTitle(),
            basicInfo.getThema(), MserverDatumZeit.parseDateTime(datumAsText, sendezeitAsText),
            Duration.of(dauerInMinutes, ChronoUnit.MINUTES));
        newFilm
            .setGeoLocations(CrawlerTool.getGeoLocations(sender, videoInfo.getDefaultVideoUrl()));
        newFilm.setWebsite(new URL(aUrlDTO.getUrl()));
        if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
          newFilm.addSubtitle(new URL(videoInfo.getSubtitleUrl()));
        }
        addUrls(newFilm, videoInfo.getVideoUrls());
        taskResults.add(newFilm);
      }
    } catch (final JsonSyntaxException jsonSyntaxException) {
      LOG.error(
          String.format(Consts.JSON_SYNTAX_ERROR, aUrlDTO.getUrl(), crawler.getSender().getName()));
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
    } catch (final Exception exception) {
      LOG.error(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName()),
          exception);
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
    }
    crawler.incrementAndGetActualCount();
    crawler.updateProgress();
  }

}
