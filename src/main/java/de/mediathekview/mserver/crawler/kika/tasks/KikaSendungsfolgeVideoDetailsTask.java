package de.mediathekview.mserver.crawler.kika.tasks;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaSendungsfolgeVideoDetailsTask extends AbstractUrlTask<Film, CrawlerUrlDTO> {
  private static final String ELEMENT_HTML_URL = "htmlUrl";
  private static final String ELEMENT_TOPLINE = "topline";
  private static final String MEDIA_TYPE_MP4 = "MP4";
  private static final String ELEMENT_BROADCAST_URL = "broadcastURL";
  private static final String ELEMENT_ASSET = "asset";
  private static final String ELEMENT_BROADCAST_DATE = "broadcastDate";
  private static final String ELEMENT_DURATION = "duration";
  private static final String ELEMENT_BROADCAST_DESCRIPTION = "broadcastDescription";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_CHANNELNAME = "channelName";
  private static final Logger LOG = LogManager.getLogger(KikaSendungsfolgeVideoDetailsTask.class);
  private static final long serialVersionUID = 6336802731231493377L;

  public KikaSendungsfolgeVideoDetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private void addFilmUrls(final Elements videoElements, final String thema, final String title,
      final Film newFilm) {
    for (final Element videoElement : videoElements) {
      final Elements mediaTypeNodes = videoElement.getElementsByTag("mediaType");
      final Elements frameWidthNodes = videoElement.getElementsByTag("frameWidth");
      final Elements fileSizeNodes = videoElement.getElementsByTag("fileSize");
      final Elements downloadUrlNodes = videoElement.getElementsByTag("progressiveDownloadUrl");

      if (!mediaTypeNodes.isEmpty() && MEDIA_TYPE_MP4.equals(mediaTypeNodes.get(0).text())
          && !frameWidthNodes.isEmpty() && !fileSizeNodes.isEmpty()
          && !downloadUrlNodes.isEmpty()) {

        final Optional<FilmUrl> newFilmUrl =
            buildFilmUrl(thema, title, downloadUrlNodes, fileSizeNodes);
        final Optional<Resolution> filmResolution = gatherResolution(thema, title, frameWidthNodes);
        if (newFilmUrl.isPresent() && filmResolution.isPresent()) {
          newFilm.addUrl(filmResolution.get(), newFilmUrl.get());
        }
      }
    }
  }

  private Optional<FilmUrl> buildFilmUrl(final String aThema, final String aTitle,
      final Elements aDownloadUrlNodes, final Elements aFileSizeNodes) {

    final String urlText = aDownloadUrlNodes.get(0).text();
    final String fileSizeText = aFileSizeNodes.get(0).text();
    try {
      return Optional.of(new FilmUrl(new URL(urlText), Long.parseLong(fileSizeText)));

    } catch (final MalformedURLException malformedURLException) {
      LOG.debug(
          String.format("The download URL \"%s\" for the film \"%s - %s\" is not a valid URL.",
              urlText, aThema, aTitle),
          malformedURLException);
    } catch (final NumberFormatException numberFormatException) {
      LOG.debug(
          String.format("The file size \"%s\" for the film \"%s - %s\" is not a valid number.",
              fileSizeText, aThema, aTitle),
          numberFormatException);
    }
    return Optional.empty();
  }

  private Optional<Resolution> gatherResolution(final String aThema, final String aTitle,
      final Elements frameWidthNodes) {
    final String frameWidthText = frameWidthNodes.get(0).text();
    try {
      return Optional.of(Resolution.getResolutionFromWidth(Integer.parseInt(frameWidthText)));
    } catch (final NumberFormatException numberFormatException) {
      LOG.debug(
          String.format("The frame width \"%s\" for the film \"%s - %s\" is not a valid number.",
              frameWidthText, aThema, aTitle),
          numberFormatException);

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

  private LocalDateTime parseTime(final Elements dateNodes, final String thema,
      final String title) {
    LocalDateTime time;
    if (!dateNodes.isEmpty() && dateNodes.get(0).text() != null) {
      time = LocalDateTime.parse(
          DateUtils.changeDateTimeForMissingISO8601Support(dateNodes.get(0).text()),
          DateTimeFormatter.ISO_DATE_TIME);
    } else {
      time = LocalDate.now().atStartOfDay();
      LOG.debug(String.format("The film \"%s - %s\" has no date so the actual date will be used.",
          thema, title));
    }
    return time;
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungsfolgeVideoDetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processUrl(final CrawlerUrlDTO aUrlDTO) {
    try {
      final Document document = Jsoup.connect(aUrlDTO.getUrl()).parser(Parser.xmlParser()).get();
      final Elements titleNodes = document.getElementsByTag(ELEMENT_TITLE);
      final Elements themaNodes = orAlternative(document, ELEMENT_CHANNELNAME, ELEMENT_TOPLINE);
      final Elements websiteUrlNodes =
          orAlternative(document, ELEMENT_BROADCAST_URL, ELEMENT_HTML_URL);
      final Elements descriptionNodes = document.getElementsByTag(ELEMENT_BROADCAST_DESCRIPTION);
      final Elements durationNodes = document.getElementsByTag(ELEMENT_DURATION);
      final Elements dateNodes = document.getElementsByTag(ELEMENT_BROADCAST_DATE);

      final Elements videoElements = document.getElementsByTag(ELEMENT_ASSET);

      if (!titleNodes.isEmpty() && !themaNodes.isEmpty() && !durationNodes.isEmpty()
          && !videoElements.isEmpty()) {
        final String thema = themaNodes.get(0).text();
        final String title = titleNodes.get(0).text();

        final LocalDateTime time = parseTime(dateNodes, thema, title);
        final Optional<Duration> dauer =
            HtmlDocumentUtils.parseDuration(durationNodes.get(0).text());

        if (dauer.isPresent()) {

          final Film newFilm =
              new Film(UUID.randomUUID(), Sender.DREISAT, title, thema, time, dauer.get());
          newFilm.setWebsite(new URL(websiteUrlNodes.get(0).text()));

          final Collection<GeoLocations> geoLocations = new ArrayList<>();
          geoLocations.add(GeoLocations.GEO_DE);
          newFilm.setGeoLocations(geoLocations);

          addFilmUrls(videoElements, thema, title, newFilm);

          if (newFilm.getUrls().isEmpty()) {
            LOG.error(String.format(
                "Can't find/build valid download URLs for the film \"%s - %s\".", thema, title));
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
          LOG.error(String.format("The duration for the film \"%s - %s\" can't be parsed: \"%s\"",
              thema, title, durationNodes.get(0).text()));
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }
      } else {
        LOG.error(String.format("The video with the URL \"%s\" has not all needed Elements",
            aUrlDTO.getUrl()));
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (

    final IOException exception) {
      LOG.fatal(String.format(
          "Something went teribble wrong on getting the film details for the Kika film \"%s\".",
          aUrlDTO.getUrl()), exception);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }

  }

}
