package mServer.crawler.sender.kika.tasks;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.*;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.*;
import mServer.crawler.sender.kika.KikaConstants;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto.FilmType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class KikaSendungsfolgeVideoDetailsTask extends AbstractUrlTask<DatenFilm, KikaCrawlerUrlDto> {

  private static final String ELEMENT_HTML_URL = "htmlUrl";
  private static final String ELEMENT_TOPLINE = "topline";
  private static final String MEDIA_TYPE_MP4 = "MP4";
  private static final String ELEMENT_BROADCAST_URL = "broadcastURL";
  private static final String ELEMENT_ASSET = "asset";
  private static final String ELEMENT_BROADCAST_DATE = "broadcastDate";
  private static final String ELEMENT_DURATION = "duration";
  private static final String ELEMENT_LENGTH = "length";
  private static final String ELEMENT_BROADCAST_SERIES = "broadcastSeriesName";
  private static final String ELEMENT_BROADCAST_NAME = "broadcastName";
  private static final String ELEMENT_BROADCAST_DESCRIPTION = "broadcastDescription";
  private static final String ELEMENT_HEADLINE = "headline";
  private static final String ELEMENT_TEASERTEXT = "teaserText";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_WEBTIME = "webTime";
  private static final String ELEMENT_CHANNELNAME = "channelName";
  private static final String TITLE_EXTENSION_SIGN_LANGUAGE = "(mit Gebärdensprache)";
  private static final String TITLE_EXTENSION_AUDIO_DESCRIPTION = "(Audiodeskription)";

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Logger LOG = LogManager.getLogger(KikaSendungsfolgeVideoDetailsTask.class);
  private static final long serialVersionUID = 6336802731231493377L;
  private static final DateTimeFormatter webTimeFormatter
          = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private transient JsoupConnection jsoupConnection;

  public KikaSendungsfolgeVideoDetailsTask(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
    jsoupConnection = new JsoupConnection();
  }

  private boolean isLong(final String number) {
    if (number == null) {
      return false;
    }
    try {
      Long.parseLong(number);
      return true;
    } catch (final NumberFormatException numberFormatException) {
      return false;
    }
  }

  private Map<Qualities, String> parseFilmUrls(
          final Elements videoElements) {

    Map<Qualities, String> videoUrls = new HashMap<>();

    final Set<KikaFilmUrlInfoDto> urlInfos = parseVideoElements(videoElements);
    for (final KikaFilmUrlInfoDto urlInfo : urlInfos) {

      if (!urlInfo.getUrl().isEmpty()
              && urlInfo.getFileType().isPresent()
              && urlInfo.getFileType().get().equalsIgnoreCase(MEDIA_TYPE_MP4)) {
        Optional<Qualities> filmResolution = getResolutionFromWidth(urlInfo);
        if (!filmResolution.isPresent()) {
          filmResolution = getResolutionFromProfile(urlInfo);
        }
        if (!filmResolution.isPresent()) {
          filmResolution = Optional.of(Qualities.SMALL);
        }

        if (!videoUrls.containsKey(filmResolution.get())) {
          videoUrls.put(filmResolution.get(), UrlUtils.addProtocolIfMissing(urlInfo.getUrl(), "https:"));
        }
      }
    }
    return videoUrls;
  }

  private Optional<Qualities> getResolutionFromProfile(final KikaFilmUrlInfoDto urlInfo) {
    final String profileName = urlInfo.getProfileName().toLowerCase();
    if (profileName.contains("low") || profileName.contains("quality = 0")) {
      return Optional.of(Qualities.SMALL);
    }
    if (profileName.contains("high")
            || profileName.contains("quality = 3")
            || profileName.contains("quality = 2")
            || profileName.contains("quality = 1")) {
      return Optional.of(Qualities.NORMAL);
    }
    if (profileName.contains("720p25")) {
      return Optional.of(Qualities.HD);
    }
    LOG.debug("KIKA: unknown profile: {}", profileName);
    return Optional.empty();
  }

  private Set<KikaFilmUrlInfoDto> parseVideoElements(final Elements videoElements) {
    final TreeSet<KikaFilmUrlInfoDto> urlInfos = new TreeSet<>(new KikaFilmUrlInfoComparator());

    videoElements.stream()
            .map(this::parseVideoElement)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(urlInfos::add);

    return urlInfos.descendingSet();
  }

  private Optional<KikaFilmUrlInfoDto> parseVideoElement(final Element aVideoElement) {
    final Elements frameWidthNodes = aVideoElement.getElementsByTag("frameWidth");
    final Elements frameHeightNodes = aVideoElement.getElementsByTag("frameHeight");
    final Elements downloadUrlNodes = aVideoElement.getElementsByTag("progressiveDownloadUrl");
    final Elements profileNameNodes = aVideoElement.getElementsByTag("profileName");
    final Elements fileSizeNode = aVideoElement.getElementsByTag("fileSize");

    if (downloadUrlNodes.isEmpty()
            || profileNameNodes.isEmpty()
            || profileNameNodes.get(0).text().startsWith("Audio")) {
      LOG.info("Missing Video element");
      return Optional.empty();
      // audio task do not have a video element
    }

    final KikaFilmUrlInfoDto info
            = new KikaFilmUrlInfoDto(downloadUrlNodes.get(0).text(), profileNameNodes.get(0).text());
    final String width = frameWidthNodes.get(0).text();
    final String height = frameHeightNodes.get(0).text();

    if (!width.isEmpty() && !height.isEmpty()) {
      info.setResolution(Integer.parseInt(width), Integer.parseInt(height));
    }

    if (!fileSizeNode.isEmpty() && isLong(fileSizeNode.get(0).text())) {
      info.setSize(Long.parseLong(fileSizeNode.get(0).text()));
    }

    return Optional.of(info);
  }

  private Optional<Qualities> getResolutionFromWidth(final KikaFilmUrlInfoDto aUrlInfo) {
    if (aUrlInfo.getWidth() >= 1280) {
      return Optional.of(Qualities.HD);
    }
    if (aUrlInfo.getWidth() > 512) {
      return Optional.of(Qualities.NORMAL);
    }
    if (aUrlInfo.getWidth() > 0) {
      return Optional.of(Qualities.SMALL);
    }
    return Optional.empty();
  }

  private Elements orAlternative(final Document aDocument, final String... aTags) {
    for (final String tag : aTags) {
      final Elements tagElements = aDocument.getElementsByTag(tag);
      if (!tagElements.isEmpty()) {
        return tagElements;
      }
    }
    return new Elements();
  }

  private LocalDateTime parseTime(
          final Elements dateNodes, final String thema, final String title) {
    LocalDateTime time;
    if (!dateNodes.isEmpty() && dateNodes.get(0).text() != null) {
      final String timeString
              = DateUtils.changeDateTimeForMissingISO8601Support(dateNodes.get(0).text());
      try {
        time = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME);
      } catch (final DateTimeException ignore) {
        time = LocalDateTime.parse(timeString, webTimeFormatter);
      }
    } else {
      time = LocalDate.now().atStartOfDay();
      LOG.debug("The film \"{} - {}\" has no date so the actual date will be used.", thema, title);
    }
    return time;
  }

  @Override
  protected AbstractUrlTask<DatenFilm, KikaCrawlerUrlDto> createNewOwnInstance(
          final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlsToCrawl) {
    return new KikaSendungsfolgeVideoDetailsTask(crawler, aUrlsToCrawl);
  }

  @Override
  protected void processElement(final KikaCrawlerUrlDto urlDto) {
    if (Config.getStop()) {
      return;
    }

    try {
      final Document document
              = jsoupConnection.getDocumentTimeoutAfterAlternativeDocumentType(
              urlDto.getUrl(),
              (int) TimeUnit.SECONDS.toMillis(KikaConstants.SOCKET_TIMEOUT),
              Parser.xmlParser());
      final Elements titleNodes = orAlternative(document, ELEMENT_BROADCAST_NAME, ELEMENT_TITLE);
      final Elements themaNodes
              = orAlternative(
              document,
              ELEMENT_BROADCAST_SERIES,
              ELEMENT_CHANNELNAME,
              ELEMENT_TOPLINE,
              ELEMENT_HEADLINE);
      final Elements websiteUrlNodes
              = orAlternative(document, ELEMENT_BROADCAST_URL, ELEMENT_HTML_URL);
      final Elements descriptionNodes
              = orAlternative(document, ELEMENT_BROADCAST_DESCRIPTION, ELEMENT_TEASERTEXT);
      final Elements durationNodes = document.getElementsByTag(ELEMENT_DURATION);
      final Elements durationNodesC8 = document.getElementsByTag(ELEMENT_LENGTH);
      final Elements dateNodes = orAlternative(document, ELEMENT_BROADCAST_DATE, ELEMENT_WEBTIME);

      final Elements videoElements = document.getElementsByTag(ELEMENT_ASSET);

      if (!titleNodes.isEmpty()
              && !themaNodes.isEmpty()
              && !durationNodes.isEmpty()
              && !videoElements.isEmpty()) {
        final String thema = themaNodes.get(0).text();
        final String title = getTitle(titleNodes.get(0).text(), urlDto.getFilmType());

        final LocalDateTime time = parseTime(dateNodes, thema, title);
        Optional<Duration> dauer = HtmlDocumentUtils.parseDuration(durationNodes.get(0).text());
        if (!dauer.isPresent()) {
          dauer = Optional.of(Duration.ofSeconds(Integer.parseInt(durationNodesC8.get(0).text())));
        }
        String description = "";
        if (!descriptionNodes.isEmpty()) {
          description = descriptionNodes.get(0).text();
        }

        final Map<Qualities, String> videoUrls = parseFilmUrls(videoElements);
        if (!videoUrls.containsKey(Qualities.NORMAL)) {
          Log.errorLog(984513215, "no video url found for " + urlDto.getUrl());
          return;
        }

        String dateValue = time.format(DATE_FORMAT);
        String timeValue = time.format(TIME_FORMAT);

        DatenFilm film = new DatenFilm(Const.KIKA, thema, websiteUrlNodes.get(0).text(), title, videoUrls.get(Qualities.NORMAL), "",
                dateValue, timeValue, dauer.orElse(Duration.ZERO).getSeconds(), description);
        if (videoUrls.containsKey(Qualities.SMALL)) {
          CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
        }
        if (videoUrls.containsKey(Qualities.HD)) {
          CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
        }

        taskResults.add(film);
      } else {
        LOG.error("The video with the URL \"{}\" has not all needed Elements", urlDto.getUrl());
        Log.errorLog(3859753, "The video with the URL " + urlDto.getUrl() + " has not all needed Elements");
      }
    } catch (final IOException exception) {
      LOG.fatal(
              String.format(
                      "Something went teribble wrong on getting the film details for the Kika film \"%s\".",
                      urlDto.getUrl()),
              exception);
      Log.errorLog(23842323, exception, urlDto.getUrl());
    }
  }

  private String getTitle(String text, FilmType filmType) {
    if (filmType == FilmType.SIGN_LANGUAGE) {
      return String.format("%s %s", text, TITLE_EXTENSION_SIGN_LANGUAGE);
    }
    if (filmType == FilmType.AUDIO_DESCRIPTION) {
      return String.format("%s %s", text, TITLE_EXTENSION_AUDIO_DESCRIPTION);
    }
    return text;
  }

  public JsoupConnection getJsoupConnection() {
    return jsoupConnection;
  }

  public void setJsoupConnection(final JsoupConnection jsoupConnection) {
    this.jsoupConnection = jsoupConnection;
  }
}
