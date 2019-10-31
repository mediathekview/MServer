package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Gathers the URLs needed to get the "verpasste Sendungen".
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Jabber:</b> nicklas2751@elaon.de<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class KikaSendungVerpasstOverviewUrlTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(KikaSendungVerpasstOverviewUrlTask.class);
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final String ATTRIBUTE_DATA_CTRL_IPG_TRIGGER = "data-ctrl-ipg-trigger";
  private static final String URL_SELECTOR = ".ipgControl .box";
  private final AbstractCrawler crawler;
  private final LocalDateTime today;

  JsoupConnection jsoupConnection;

  public KikaSendungVerpasstOverviewUrlTask(
      final AbstractCrawler aCrawler, final LocalDateTime aToday) {
    crawler = aCrawler;
    today = aToday;
    this.jsoupConnection = new JsoupConnection();
  }

  @Override
  public Set<CrawlerUrlDTO> call() {
    // In a own variable so the method isn't called for every item.
    final Set<String> allowedDateStrings = getAllowedDateStrings();

    // Gathers the raw "Sendung Verpasst" overview page URLs, filters them by checking if any of the
    // allowed date strings is contained and then build CrawlerUrlDTOs of the filtered result.
    return gatherAllRawSendungVerpasstOverviewPageUrls().stream()
        .filter(u -> allowedDateStrings.parallelStream().anyMatch(u::contains))
        .map(CrawlerUrlDTO::new)
        .collect(Collectors.toSet());
  }

  private Set<String> gatherAllRawSendungVerpasstOverviewPageUrls() {
    final Set<String> rawSendungVerpasstOverviewPageUrls = new HashSet<>();
    try {
      final Document document = jsoupConnection.getDocumentTimeoutAfter(
          KikaConstants.URL_DAY_PAGE,
          (int)
              TimeUnit.SECONDS.toMillis(
                  crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
      for (final Element urlElement : document.select(URL_SELECTOR)) {
        final Optional<String> rawSendungVerpasstOverviewPageUrl =
            KikaHelper.gatherIpgTriggerUrlFromElement(
                urlElement, ATTRIBUTE_DATA_CTRL_IPG_TRIGGER, KikaConstants.BASE_URL);
        rawSendungVerpasstOverviewPageUrl.ifPresent(rawSendungVerpasstOverviewPageUrls::add);
      }
    } catch (final HttpStatusException httpStatusError) {
      LOG.fatal(
          "Something went teribble wrong on loading the KIKA base \"Sendung Verpasst\" overview page.");
      crawler.printMessage(
          ServerMessages.CRAWLER_DOCUMENT_LOAD_ERROR,
          crawler.getSender().getName(),
          KikaConstants.URL_DAY_PAGE,
          httpStatusError.getStatusCode());
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something went teribble wrong on loading the KIKA base \"Sendung Verpasst\" overview page.");
      crawler.printErrorMessage();
    }
    return rawSendungVerpasstOverviewPageUrls;
  }

  private Set<String> getAllowedDateStrings() {
    final Set<String> dateStrings = new HashSet<>();
    for (int i = 0; i < crawler.getCrawlerConfig().getMaximumDaysForSendungVerpasstSection(); i++) {
      dateStrings.add(today.minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    for (int i = 1;
        i <= crawler.getCrawlerConfig().getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      dateStrings.add(today.plus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    return dateStrings;
  }

  public JsoupConnection getJsoupConnection() {
    return jsoupConnection;
  }

  public void setJsoupConnection(JsoupConnection jsoupConnection) {
    this.jsoupConnection = jsoupConnection;
  }
}
