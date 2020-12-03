package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.*;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmUrlInfoDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class KikaSendungsfolgeVideoDetailsTask extends AbstractUrlTask<Film, CrawlerUrlDTO> {

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
  private static final Logger LOG = LogManager.getLogger(KikaSendungsfolgeVideoDetailsTask.class);
  private static final long serialVersionUID = 6336802731231493377L;
  private static final DateTimeFormatter webTimeFormatter =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private transient JsoupConnection jsoupConnection;

  public KikaSendungsfolgeVideoDetailsTask(
      final AbstractCrawler aCrawler, final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    jsoupConnection = new JsoupConnection();
  }

  private void addFilmUrls(
      final Elements videoElements, final String thema, final String title, final Film newFilm) {
    final Set<KikaFilmUrlInfoDto> urlInfos = parseVideoElements(videoElements);
    for (final KikaFilmUrlInfoDto urlInfo : urlInfos) {

      if (!urlInfo.getUrl().isEmpty()
          && urlInfo.getFileType().isPresent()
          && urlInfo.getFileType().get().equalsIgnoreCase(MEDIA_TYPE_MP4)) {
        Optional<Resolution> filmResolution = getResolutionFromWidth(urlInfo);
        if (filmResolution.isEmpty()) {
          filmResolution = getResolutionFromProfile(urlInfo);
        }
        if (filmResolution.isEmpty()) {
          filmResolution = Optional.of(Resolution.SMALL);
        }
        try {
          if (newFilm.getUrl(filmResolution.get()) == null) {
            newFilm.addUrl(filmResolution.get(), new FilmUrl(urlInfo.getUrl(), urlInfo.getSize()));
          }
        } catch (final MalformedURLException e) {
          LOG.debug(
              String.format(
                  "The download URL \"%s\" for the film \"%s - %s\" is not a valid URL.",
                  urlInfo.getUrl(), thema, title),
              e);
        }
      }
    }
  }

  private Optional<Resolution> getResolutionFromProfile(final KikaFilmUrlInfoDto urlInfo) {
    final String profileName = urlInfo.getProfileName().toLowerCase();
    if (profileName.contains("low")) {
      return Optional.of(Resolution.SMALL);
    }
    if (profileName.contains("high")) {
      return Optional.of(Resolution.NORMAL);
    }
    if (profileName.contains("720p25")) {
      return Optional.of(Resolution.HD);
    }
    LOG.debug("KIKA: unknown profile: {}", profileName);
    return Optional.empty();
  }

  private Set<KikaFilmUrlInfoDto> parseVideoElements(final Elements aVideoElements) {
    final TreeSet<KikaFilmUrlInfoDto> urlInfos = new TreeSet<>(new KikaFilmUrlInfoComparator());

    for (final Element videoElement : aVideoElements) {
      final KikaFilmUrlInfoDto urlInfo = parseVideoElement(videoElement);
      if (urlInfo != null) {
        urlInfos.add(urlInfo);
      }
    }

    return urlInfos.descendingSet();
  }

  private KikaFilmUrlInfoDto parseVideoElement(final Element aVideoElement) {
    final Elements frameWidthNodes = aVideoElement.getElementsByTag("frameWidth");
    final Elements frameHeightNodes = aVideoElement.getElementsByTag("frameHeight");
    final Elements downloadUrlNodes = aVideoElement.getElementsByTag("progressiveDownloadUrl");
    final Elements profileNameNodes = aVideoElement.getElementsByTag("profileName");
    final Elements fileSizeNode = aVideoElement.getElementsByTag("fileSize");
    
    if (downloadUrlNodes.size() == 0 || profileNameNodes.size() == 0 || profileNameNodes.get(0).text().startsWith("Audio")) {
      LOG.info("Missnig Video element");
      return null;
      // audio task do not have a video element
    }
    final KikaFilmUrlInfoDto info =
        new KikaFilmUrlInfoDto(downloadUrlNodes.get(0).text(), profileNameNodes.get(0).text());
    String width = frameWidthNodes.get(0).text();
    String height = frameHeightNodes.get(0).text();

    if (!width.isEmpty() && !height.isEmpty()) {
      info.setResolution(Integer.parseInt(width), Integer.parseInt(height));
    }
    
    if (fileSizeNode.size() > 0) {
      if (KikaSendungsfolgeVideoDetailsTask.isLong(fileSizeNode.get(0).text())) {
        info.setSize(Long.parseLong(fileSizeNode.get(0).text())/1024/1024);
      }
    }
    
    return info;
  }
  
  public static boolean isLong(String strNum) {
    if (strNum == null) {
        return false;
    }
    try {
        long d = Long.parseLong(strNum);
    } catch (NumberFormatException nfe) {
        return false;
    }
    return true;
}

  private Optional<Resolution> getResolutionFromWidth(final FilmUrlInfoDto aUrlInfo) {
    if (aUrlInfo.getWidth() >= 1280) {
      return Optional.of(Resolution.HD);
    }
    if (aUrlInfo.getWidth() > 512) {
      return Optional.of(Resolution.NORMAL);
    }
    if (aUrlInfo.getWidth() > 0) {
      return Optional.of(Resolution.SMALL);
    }
    return Optional.empty();
  }

  private Elements orAlternative(final Document aDocucument, final String... aTags) {
    for (final String tag : aTags) {
      final Elements tagElements = aDocucument.getElementsByTag(tag);
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
      final String timeString =
          DateUtils.changeDateTimeForMissingISO8601Support(dateNodes.get(0).text());
      try {
        time = LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME);
      } catch (DateTimeException ignore) {
        time = LocalDateTime.parse(timeString, webTimeFormatter);
      }
    } else {
      time = LocalDate.now().atStartOfDay();
      LOG.debug(
          String.format(
              "The film \"%s - %s\" has no date so the actual date will be used.", thema, title));
    }
    return time;
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungsfolgeVideoDetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processElement(final CrawlerUrlDTO urlDTO) {
    try {
      final Document document =
          jsoupConnection.getDocumentTimeoutAfterAlternativeDocumentType(
              urlDTO.getUrl(),
              (int) TimeUnit.SECONDS.toMillis(config.getSocketTimeoutInSeconds()),
              Parser.xmlParser());
      final Elements titleNodes = orAlternative(document, ELEMENT_BROADCAST_NAME, ELEMENT_TITLE);
      final Elements themaNodes =
          orAlternative(
              document,
              ELEMENT_BROADCAST_SERIES,
              ELEMENT_CHANNELNAME,
              ELEMENT_TOPLINE,
              ELEMENT_HEADLINE);
      final Elements websiteUrlNodes =
          orAlternative(document, ELEMENT_BROADCAST_URL, ELEMENT_HTML_URL);
      final Elements descriptionNodes = orAlternative(document, ELEMENT_BROADCAST_DESCRIPTION, ELEMENT_TEASERTEXT);
      final Elements durationNodes = document.getElementsByTag(ELEMENT_DURATION);
      final Elements durationNodesC8 = document.getElementsByTag(ELEMENT_LENGTH);
      final Elements dateNodes = orAlternative(document, ELEMENT_BROADCAST_DATE, ELEMENT_WEBTIME);

      final Elements videoElements = document.getElementsByTag(ELEMENT_ASSET);

      if (!titleNodes.isEmpty()
          && !themaNodes.isEmpty()
          && !durationNodes.isEmpty()
          && !videoElements.isEmpty()) {
        final String thema = themaNodes.get(0).text();
        final String title = titleNodes.get(0).text();

        final LocalDateTime time = parseTime(dateNodes, thema, title);
        Optional<Duration> dauer =
            HtmlDocumentUtils.parseDuration(durationNodes.get(0).text());
        if (dauer.isEmpty()) {
          dauer = Optional.of(Duration.ofSeconds(Integer.parseInt(durationNodesC8.get(0).text())));
        }

        if (dauer.isPresent()) {

          final Film newFilm =
              new Film(UUID.randomUUID(), Sender.KIKA, title, thema, time, dauer.get());
          newFilm.setWebsite(new URL(websiteUrlNodes.get(0).text()));

          addFilmUrls(videoElements, thema, title, newFilm);
          addGeo(newFilm);

          if (newFilm.getUrls().isEmpty()) {
            LOG.error(
                String.format(
                    "Can't find/build valid download URLs for the film \"%s - %s\".",
                    thema, title));
            crawler.incrementAndGetErrorCount();
            crawler.updateProgress();
          } else {

            if (!descriptionNodes.isEmpty()) {
              newFilm.setBeschreibung(descriptionNodes.get(0).text());
            }
            taskResults.add(newFilm);
            crawler.incrementAndGetActualCount();
            crawler.updateProgress();
          }
        } else {
          LOG.error(
              String.format(
                  "The duration for the film \"%s - %s\" can't be parsed: \"%s\"",
                  thema, title, durationNodes.get(0).text()));
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }
      } else {
        LOG.error(
            String.format(
                "The video with the URL \"%s\" has not all needed Elements", urlDTO.getUrl()));
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (final IOException exception) {
      LOG.fatal(
          String.format(
              "Something went teribble wrong on getting the film details for the Kika film \"%s\".",
              urlDTO.getUrl()),
          exception);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }
  }

  private void addGeo(final Film newFilm) {
    final Optional<FilmUrl> url = newFilm.getDefaultUrl();
    if (!url.isPresent()) {
      return;
    }

    GeoLocations geoLocation = GeoLocations.GEO_NONE;
    if (url.get().getUrl().getHost().contains("pmdgeokika")) {
      geoLocation = GeoLocations.GEO_DE;
    }

    final Collection<GeoLocations> geoLocations = new ArrayList<>();
    geoLocations.add(geoLocation);
    newFilm.setGeoLocations(geoLocations);
  }

  public JsoupConnection getJsoupConnection() {
    return jsoupConnection;
  }

  public void setJsoupConnection(final JsoupConnection jsoupConnection) {
    this.jsoupConnection = jsoupConnection;
  }
}
