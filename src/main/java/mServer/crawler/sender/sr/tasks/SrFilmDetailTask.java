package mServer.crawler.sender.sr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.sr.SrTopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.json.ArdVideoInfoDto;
import mServer.crawler.sender.ard.json.ArdVideoInfoJsonDeserializer;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.DateUtils;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.base.HtmlDocumentUtils;
import mServer.crawler.sender.base.AbstractDocumentTask;

public class SrFilmDetailTask extends AbstractDocumentTask<DatenFilm, SrTopicUrlDTO> {

  private static final org.apache.logging.log4j.Logger LOG
          = LogManager.getLogger(SrFilmDetailTask.class);

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final DateTimeFormatter DATE_TIME_FORMATTER
          = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMANY);
  private static final DateTimeFormatter DATE_TIME_FORMATTER_ENG
          = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.GERMANY);

  private static final String CONTENT_SELECTOR = "div.article__content";
  private static final String TITLE_SELECTOR = CONTENT_SELECTOR + " > div > h3";
  private static final String DETAILS_SELECTOR = CONTENT_SELECTOR + " > div > p";
  private static final String DESCRIPTION_SELECTOR = "h1.background-title";
  private static final String VIDEO_DETAIL_ATTRIBUTE = "data-mediacollection-ardplayer";
  private static final String VIDEO_DETAIL_SELECTOR = "div[" + VIDEO_DETAIL_ATTRIBUTE + "]";

  public SrFilmDetailTask(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<SrTopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private static Optional<String> parseDescription(final Document aDocument) {
    final Elements x = aDocument.select(DESCRIPTION_SELECTOR);
    if (x.size() > 0) {
      final Node node = x.first().nextSibling();
      return Optional.of(node.toString());
    }

    return Optional.empty();
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    final Optional<String> date = getDetailElement(aDocument, 1);
    if (date.isPresent()) {
      final String isoDate = DateUtils.changeDateTimeForMissingISO8601Support(date.get().trim());
      try {
        final LocalDateTime localDate
                = LocalDateTime.parse(isoDate + " 00:00", DATE_TIME_FORMATTER);
        return Optional.of(localDate);
      } catch (final DateTimeParseException e) {
        try {
          final LocalDateTime localDate
                  = LocalDateTime.parse(isoDate + " 00:00", DATE_TIME_FORMATTER_ENG);
          return Optional.of(localDate);
        } catch (final DateTimeParseException e1) {
          LOG.fatal(e1);
        }
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final Document aDocument) {
    final Optional<String> duration = getDetailElement(aDocument, 2);
    if (duration.isPresent()) {
      final String[] parts = duration.get().replace("Dauer:", "").trim().split(":");
      if (parts.length == 3) {
        return Optional.of(
                Duration.ofHours(Long.parseLong(parts[0]))
                        .plusMinutes(Long.parseLong(parts[1]))
                        .plusSeconds(Long.parseLong(parts[2])));
      } else {
        LOG.debug("SrFilmDetailTask: unknown duration part count: " + duration.get());
      }
    }

    return Optional.empty();
  }

  private static Optional<String> getDetailElement(final Document aDocument, final int index) {
    final Optional<String> details
            = HtmlDocumentUtils.getElementString(DETAILS_SELECTOR, aDocument);
    if (details.isPresent()) {
      final String[] parts = details.get().split("\\|");
      if (parts.length == 4) {
        return Optional.of(parts[index]);
      } else {
        LOG.debug("SrFilmDetailTask: unknown details part count: " + details.get());
      }
    }

    return Optional.empty();
  }

  private static String addMissingProtocol(final String aUrl) {
    if (aUrl.startsWith("//")) {
      return "https:" + aUrl;
    }

    return aUrl;
  }

  @Override
  protected void processDocument(final SrTopicUrlDTO aUrlDTO, final Document aDocument) {

    if (!isRelevantType(aDocument)) {
      return;
    }

    final Optional<String> title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR, aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description = parseDescription(aDocument);

    final Optional<ArdVideoInfoDto> videoInfoOptional = parseUrls(aDocument);
    if (videoInfoOptional.isPresent() && title.isPresent()) {
      final ArdVideoInfoDto videoInfo = videoInfoOptional.get();

      String dateValue = time.get().format(DATE_FORMAT);
      String timeValue = time.get().format(TIME_FORMAT);

      Map<Qualities, String> videoUrls = videoInfo.getVideoUrls();

      DatenFilm film = new DatenFilm(Const.SR, aUrlDTO.getTheme(), "", title.orElse(""), videoInfo.getDefaultVideoUrl(), "", dateValue, timeValue, duration.orElse(Duration.ZERO).getSeconds(), description.orElse(""));
      if (videoUrls.containsKey(Qualities.SMALL)) {
        CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
      }
      if (videoUrls.containsKey(Qualities.HD)) {
        CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
      }
      if (videoInfo.getSubtitleUrlOptional().isPresent()) {
        CrawlerTool.addUrlSubtitle(film, videoInfo.getSubtitleUrl());
      }

      taskResults.add(film);
    } else {
      LOG.error("SrFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
      Log.errorLog(74856890, "SrFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
    }
  }

  @Override
  protected AbstractUrlTask<DatenFilm, SrTopicUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<SrTopicUrlDTO> aURLsToCrawl) {
    return new SrFilmDetailTask(crawler, aURLsToCrawl);
  }

  /**
   * checks whether the multimedia type is relevant
   *
   * @param aDocument the html document
   * @return true if document is relevant
   */
  private boolean isRelevantType(final Document aDocument) {
    // ignore all documents who does not contain any video
    final Optional<String> type = getDetailElement(aDocument, 0);
    return type.isPresent() && type.get().toLowerCase().contains("video");
  }

  private Optional<ArdVideoInfoDto> parseUrls(final Document aDocument) {
    final Optional<String> videoDetailUrl
            = HtmlDocumentUtils.getElementAttributeString(
                    VIDEO_DETAIL_SELECTOR, VIDEO_DETAIL_ATTRIBUTE, aDocument);
    if (videoDetailUrl.isPresent()) {
      final Gson gson
              = new GsonBuilder()
                      .registerTypeAdapter(ArdVideoInfoDto.class, new ArdVideoInfoJsonDeserializer())
                      .create();

      String url = videoDetailUrl.get();
      url = addMissingProtocol(url);

      try {
        final ArdVideoInfoDto dto
                = gson.fromJson(new InputStreamReader(new URL(url).openStream()), ArdVideoInfoDto.class);
        return Optional.of(dto);
      } catch (final IOException ex) {
        LOG.fatal(ex);
      }
    }

    return Optional.empty();
  }
}
