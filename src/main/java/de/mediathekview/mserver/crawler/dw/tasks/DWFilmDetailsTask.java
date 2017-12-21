package de.mediathekview.mserver.crawler.dw.tasks;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.DwCrawler;
import de.mediathekview.mserver.crawler.dw.parser.DWDownloadUrlsParser;
import mServer.crawler.CrawlerTool;

public class DWFilmDetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {
  private static final String URL_SPLITTERATOR = "/";
  private static final long serialVersionUID = 7992707335502505844L;
  private static final Logger LOG = LogManager.getLogger(DWFilmDetailsTask.class);
  private static final String ELEMENT_THEMA = ".artikel";
  private static final String ELEMENT_TITEL = ".group h1";
  private static final String ELEMENT_DATUM = ".group li:eq(0)";
  private static final String ELEMENT_DAUER = ".group li:eq(1)";
  private static final String DOWNLOAD_DETAILS_URL_TEMPLATE =
      DwCrawler.BASE_URL + "/playersources/%s";
  private static final String CHAR_TO_REMOVE_FROM_PAGE_ID = "a";

  private static final String DATE_REGEX_PATTERN = "(?<=Datum\\s)[\\.\\d]+";

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMAN);

  public DWFilmDetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private void addDownloadUrls(final CrawlerUrlDTO aUrlDTO, final Film film) {
    final String pageId =
        aUrlDTO.getUrl().substring(aUrlDTO.getUrl().lastIndexOf(URL_SPLITTERATOR));
    final String videoId = pageId.replaceFirst(CHAR_TO_REMOVE_FROM_PAGE_ID, "");
    final String downloadUrl = String.format(DOWNLOAD_DETAILS_URL_TEMPLATE, videoId);
    try {
      final WebTarget target = ClientBuilder.newClient().target(new URL(downloadUrl).toString());
      final String response = target.request(MediaType.APPLICATION_JSON_TYPE).get(String.class);

      final Type urlMapType = new TypeToken<Map<Resolution, FilmUrl>>() {}.getType();
      final Gson gson =
          new GsonBuilder().registerTypeAdapter(urlMapType, new DWDownloadUrlsParser()).create();

      film.addAllUrls(gson.fromJson(response, urlMapType));
    } catch (final MalformedURLException malformedURLException) {
      LOG.error(
          String.format("Something wen't wrong on building a download url \"%s\".", downloadUrl));
    }
  }

  private Optional<LocalDate> parseDate(final Optional<String> aDateText) {
    final Matcher dateMatcher = Pattern.compile(DATE_REGEX_PATTERN).matcher(aDateText.get());
    if (dateMatcher.find()) {
      return Optional.of(LocalDate.parse(dateMatcher.group(), DATE_FORMATTER));
    }
    return Optional.empty();
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DWFilmDetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Optional<String> titel = HtmlDocumentUtils.getElementString(ELEMENT_TITEL, aDocument);
    final Optional<String> thema = HtmlDocumentUtils.getElementString(ELEMENT_THEMA, aDocument);
    final Optional<String> dateText = HtmlDocumentUtils.getElementString(ELEMENT_DATUM, aDocument);
    final Optional<String> dauerText = HtmlDocumentUtils.getElementString(ELEMENT_DAUER, aDocument);
    final Optional<Duration> dauer;
    if (dauerText.isPresent()) {
      dauer = HtmlDocumentUtils.parseDuration(dauerText);
    } else {
      dauer = Optional.empty();
    }

    if (titel.isPresent()) {
      if (thema.isPresent()) {
        if (dauer.isPresent()) {
          final Optional<LocalDate> time = parseDate(dateText);

          try {
            final Film newFilm = new Film(UUID.randomUUID(), crawler.getSender(), titel.get(),
                thema.get(), time.orElse(LocalDate.now()).atStartOfDay(), dauer.get());
            newFilm.setWebsite(new URL(aUrlDTO.getUrl()));
            addDownloadUrls(aUrlDTO, newFilm);

            final Optional<FilmUrl> defaultUrl = newFilm.getDefaultUrl();
            if (defaultUrl.isPresent()) {
              newFilm.setGeoLocations(CrawlerTool.getGeoLocations(crawler.getSender(),
                  defaultUrl.get().getUrl().toString()));
            }

            taskResults.add(newFilm);
            crawler.incrementAndGetActualCount();
            crawler.updateProgress();
          } catch (final MalformedURLException malformedURLException) {
            LOG.fatal("The website URL can't be parsed.", malformedURLException);
            crawler.printInvalidUrlErrorMessage(aUrlDTO.getUrl());
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
