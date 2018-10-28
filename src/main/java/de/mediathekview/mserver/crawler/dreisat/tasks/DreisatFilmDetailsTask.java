package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dreisat.parser.DreisatFilmDetailsReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DreisatFilmDetailsTask extends AbstractUrlTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(DreisatFilmDetailsTask.class);
  private static final long serialVersionUID = -7520416794362009338L;
  private static final String XML_SERVICE_URL_PATTERN =
      "%s/mediathek/xmlservice.php/v3/web/beitragsDetails?id=%d";
  private static final String ID_URL_REGEX_PATTERN = "(?<=obj=)\\d+";
  private String baseUrl;
  private String baseUrlTmd;

  public DreisatFilmDetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final String aBaseUrl, final String aBaseUrlTmd) {
    super(aCrawler, aUrlToCrawlDTOs);
    baseUrl = aBaseUrl;
    baseUrlTmd = aBaseUrlTmd;
  }

  private Optional<Integer> getIdFromUrl(final String aUrl) {
    final Matcher idMatcher = Pattern.compile(ID_URL_REGEX_PATTERN).matcher(aUrl);
    if (idMatcher.find()) {
      return Optional.of(Integer.parseInt(idMatcher.group()));
    }
    return Optional.empty();
  }

  private Optional<URL> getXMLUrl(final String aUrl) {
    final Optional<Integer> id = getIdFromUrl(aUrl);
    if (id.isPresent()) {
      try {
        return Optional.of(new URL(String.format(XML_SERVICE_URL_PATTERN, baseUrl, id.get())));
      } catch (final MalformedURLException malformedURLException) {
        LOG.fatal("Something went terrible wrong on getting the film details for 3Sat.",
            malformedURLException);
        crawler.incrementAndGetErrorCount();
        crawler.printErrorMessage();
      }
    }
    return Optional.empty();
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DreisatFilmDetailsTask(crawler, aURLsToCrawl, baseUrl, baseUrlTmd);
  }

  @Override
  protected void processElement(final CrawlerUrlDTO aUrlDTO) {
    Optional<Film> newFilm;
    try {
      final Optional<URL> xmlUrl = getXMLUrl(aUrlDTO.getUrl());
      if (xmlUrl.isPresent()) {
        newFilm = new DreisatFilmDetailsReader(crawler, xmlUrl.get(), new URL(aUrlDTO.getUrl()), baseUrlTmd)
            .readDetails();
        if (newFilm.isPresent()) {
          taskResults.add(newFilm.get());
          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
        }
      }
    } catch (final MalformedURLException malformedURLException) {
      LOG.fatal("Something went terrible wrong on getting the film details for 3Sat.",
          malformedURLException);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }
  }

}
