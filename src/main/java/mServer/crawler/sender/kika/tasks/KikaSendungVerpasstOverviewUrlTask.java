package mServer.crawler.sender.kika.tasks;

import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.kika.KikaConstants;
import org.jsoup.HttpStatusException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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

/**
 * Gathers the URLs needed to get the "verpasste Sendungen".
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class KikaSendungVerpasstOverviewUrlTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final String ATTRIBUTE_DATA_CTRL_IPG_TRIGGER = "data-ctrl-ipg-trigger";
  private static final String URL_SELECTOR = ".ipgControl .box";
  private static final int MAX_DAYS_PAST = 7;
  private static final int MAX_DAYS_FUTURE = 14;

  private final MediathekReader crawler;
  private final LocalDateTime today;
  JsoupConnection jsoupConnection;

  public KikaSendungVerpasstOverviewUrlTask(
      final MediathekReader aCrawler, final LocalDateTime aToday) {
    crawler = aCrawler;
    today = aToday;
      jsoupConnection = new JsoupConnection();
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
      final Document document =
          jsoupConnection.getDocumentTimeoutAfter(
              KikaConstants.URL_DAY_PAGE,
              (int)
                  TimeUnit.SECONDS.toMillis(KikaConstants.SOCKET_TIMEOUT));
      for (final Element urlElement : document.select(URL_SELECTOR)) {
        final Optional<String> rawSendungVerpasstOverviewPageUrl =
            KikaHelper.gatherIpgTriggerUrlFromElement(
                urlElement, ATTRIBUTE_DATA_CTRL_IPG_TRIGGER, KikaConstants.BASE_URL);
        rawSendungVerpasstOverviewPageUrl.ifPresent(rawSendungVerpasstOverviewPageUrls::add);
      }
    } catch (final HttpStatusException httpStatusError) {
      Log.errorLog(738439, "error loading page " + KikaConstants.URL_DAY_PAGE + ": " + httpStatusError.getStatusCode());
    } catch (final IOException ioException) {
      Log.errorLog(34822332, ioException);
    }
    return rawSendungVerpasstOverviewPageUrls;
  }

  private Set<String> getAllowedDateStrings() {
    final Set<String> dateStrings = new HashSet<>();
    for (int i = 0; i < MAX_DAYS_PAST; i++) {
      dateStrings.add(today.minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    for (int i = 1;
        i <= MAX_DAYS_FUTURE;
        i++) {
      dateStrings.add(today.plus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    return dateStrings;
  }

  public JsoupConnection getJsoupConnection() {
    return jsoupConnection;
  }

  public void setJsoupConnection(final JsoupConnection jsoupConnection) {
    this.jsoupConnection = jsoupConnection;
  }
}
