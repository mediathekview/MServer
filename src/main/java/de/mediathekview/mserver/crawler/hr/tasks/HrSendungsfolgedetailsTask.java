package de.mediathekview.mserver.crawler.hr.tasks;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import mServer.crawler.CrawlerTool;

public class HrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {
  private static final String SPLITTED_NUMBERS_REGEX_PATTERN = "$1:$2";
  private static final String SPLIT_NUMBERS_REGEX_PATTERN = "(\\+\\d{1,2})(\\d{1,2})";
  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_SRC = "src";
  private static final Logger LOG = LogManager.getLogger(HrSendungsfolgedetailsTask.class);
  private static final long serialVersionUID = 6138774185290017974L;
  private static final String THEMA_SELECTOR = ".c-programHeader__headline.text__headline";
  private static final String TITEL_SELECTOR = ".c-programHeader__subline.text__topline";
  private static final String DATE_TIME_SELECTOR = ".c-programHeader__mediaWrapper time";
  private static final String DAUER_SELECTOR = ".c-programHeader__mediaWrapper .mediaInfo__byline";
  private static final String BESCHREIBUNG_SELECTOR = ".copytext__text.copytext__text strong";
  private static final String VIDEO_URL_SELECTOR = ".c-programHeader__mediaWrapper source";
  private static final String UT_URL_SELECTOR = ".c-programHeader__mediaWrapper track";

  public HrSendungsfolgedetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }


  // Java 8 misses a ISO 8601 support. See:
  // https://stackoverflow.com/questions/2201925/converting-iso-8601-compliant-string-to-java-util-date
  private String changeDateTimeForMissingISO8601Support(final String aDateTimeString) {
    return aDateTimeString.replaceAll(SPLIT_NUMBERS_REGEX_PATTERN, SPLITTED_NUMBERS_REGEX_PATTERN);
  }

  private Optional<LocalDateTime> parseDate(final Optional<String> aDateTimeText) {
    if (aDateTimeText.isPresent()) {
      final String fixedDateTimeText = changeDateTimeForMissingISO8601Support(aDateTimeText.get());
      try {
        return Optional.of(LocalDateTime.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE_TIME));
      } catch (final DateTimeParseException dateTimeParseException) {
        try {
          LOG.debug(String.format(
              "Can't parse a date time for HR: \"%s\" now trying to parse only a Date.",
              fixedDateTimeText), dateTimeParseException);
          return Optional
              .of(LocalDate.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } catch (final DateTimeParseException dateTimeParseException2) {
          LOG.error(String.format("Can't parse either a date time nor only a date for HR: \"%s\"",
              fixedDateTimeText), dateTimeParseException2);
        }
      }
    }
    return Optional.empty();
  }


  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new HrSendungsfolgedetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Optional<String> titel = HtmlDocumentUtils.getElementString(TITEL_SELECTOR, aDocument);
    final Optional<String> thema = HtmlDocumentUtils.getElementString(THEMA_SELECTOR, aDocument);
    final Optional<String> beschreibung =
        HtmlDocumentUtils.getElementString(BESCHREIBUNG_SELECTOR, aDocument);
    final Optional<String> videoUrlText =
        HtmlDocumentUtils.getElementAttributeString(VIDEO_URL_SELECTOR, ATTRIBUTE_SRC, aDocument);
    final Optional<String> untertitelUrlText =
        HtmlDocumentUtils.getElementAttributeString(UT_URL_SELECTOR, ATTRIBUTE_SRC, aDocument);
    final Optional<String> dateTimeText = HtmlDocumentUtils
        .getElementAttributeString(DATE_TIME_SELECTOR, ATTRIBUTE_DATETIME, aDocument);
    final Optional<String> dauerText =
        HtmlDocumentUtils.getElementString(DAUER_SELECTOR, aDocument);
    final Optional<Duration> dauer;
    if (dauerText.isPresent()) {
      dauer = HtmlDocumentUtils.parseDuration(dauerText);
    } else {
      dauer = Optional.empty();
    }

    if (titel.isPresent()) {
      if (thema.isPresent()) {
        if (dauer.isPresent()) {
          if (videoUrlText.isPresent()) {
            final Optional<LocalDateTime> time = parseDate(dateTimeText);

            try {
              final Film newFilm = new Film(UUID.randomUUID(), crawler.getSender(), titel.get(),
                  thema.get(), time.orElse(LocalDate.now().atStartOfDay()), dauer.get());
              newFilm.setWebsite(new URL(aUrlDTO.getUrl()));
              if (beschreibung.isPresent()) {
                newFilm.setBeschreibung(beschreibung.get());
              }

              // Actually HR has only Normal ¯\_(ツ)_/¯
              newFilm.addUrl(Resolution.NORMAL,
                  CrawlerTool.uriToFilmUrl(new URL(videoUrlText.get())));

              if (untertitelUrlText.isPresent()) {
                newFilm.addSubtitle(new URL(untertitelUrlText.get()));
              }

              final Optional<FilmUrl> defaultUrl = newFilm.getDefaultUrl();
              if (defaultUrl.isPresent()) {
                newFilm.setGeoLocations(CrawlerTool.getGeoLocations(crawler.getSender(),
                    defaultUrl.get().getUrl().toString()));
              }

              taskResults.add(newFilm);
              crawler.incrementAndGetActualCount();
              crawler.updateProgress();
            } catch (final MalformedURLException malformedURLException) {
              LOG.fatal("A HR URL can't be parsed.", malformedURLException);
              crawler.printErrorMessage();
              crawler.incrementAndGetErrorCount();
              crawler.updateProgress();
            }
          } else {
            crawler.printMissingElementErrorMessage("Video-Url");
          }
        } else {
          crawler.printMissingElementErrorMessage("Dauer");
        }
      } else {
        crawler.printMissingElementErrorMessage("Titel");
      }
    } else {
      crawler.printMissingElementErrorMessage("Thema");
    }
  }
}
