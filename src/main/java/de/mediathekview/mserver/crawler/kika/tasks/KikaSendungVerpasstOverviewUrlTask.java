package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * Gathers the URLs needed to get the "verpasste Sendungen".
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 * <b>Mail:</b> nicklas@wiegandt.eu<br>
 * <b>Jabber:</b> nicklas2751@elaon.de<br>
 * <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class KikaSendungVerpasstOverviewUrlTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(KikaSendungVerpasstOverviewUrlTask.class);
  private static final String GATHER_URL_REGEX_PATTERN = "(?<=url':')[^']*";
  private static final String ATTRIBUTE_DATA_CTRL_IPG_TRIGGER = "data-ctrl-ipg-trigger";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("ddMMyyyy");
  private static final String URL_SELECTOR = ".ipgControl .box";
  private final AbstractCrawler crawler;

  public KikaSendungVerpasstOverviewUrlTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<CrawlerUrlDTO> call() {
    // In a own variable so the method isn't called for every item.
    final Set<String> allowedDateStrings = getAllowedDateStrings();

    // Gathers the raw "Sendung Verpasst" overview page URLs, filters them by checking if any of the
    // allowed date strings is contained and then build CrawlerUrlDTOs of the filtered result.
    return gatherAllRawSendungVerpasstOverviewPageUrls().stream()
        .filter(u -> allowedDateStrings.parallelStream().anyMatch(u::contains))
        .map(CrawlerUrlDTO::new).collect(Collectors.toSet());
  }

  private Set<String> gatherAllRawSendungVerpasstOverviewPageUrls() {
    final Set<String> rawSendungVerpasstOverviewPageUrls = new HashSet<>();
    try {
      final Document document = Jsoup.connect(KikaConstants.URL_DAY_PAGE).get();
      for (final Element urlElement : document.select(URL_SELECTOR)) {
        final Optional<String> rawSendungVerpasstOverviewPageUrl =
            gatherIpgTriggerUrlFromElement(urlElement);
        rawSendungVerpasstOverviewPageUrl.ifPresent(rawSendungVerpasstOverviewPageUrls::add);
      }
    } catch (final HttpStatusException httpStatusError) {
      LOG.fatal(
          "Something went teribble wrong on loading the KIKA base \"Sendung Verpasst\" overview page.");
      crawler.printMessage(ServerMessages.CRAWLER_DOCUMENT_LOAD_ERROR,
          crawler.getSender().getName(), KikaConstants.URL_DAY_PAGE, httpStatusError.getStatusCode());
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something went teribble wrong on loading the KIKA base \"Sendung Verpasst\" overview page.");
      crawler.printErrorMessage();
    }
    return rawSendungVerpasstOverviewPageUrls;
  }

  private Optional<String> gatherIpgTriggerUrlFromElement(final Element aUrlElement) {
    if (aUrlElement.hasAttr(ATTRIBUTE_DATA_CTRL_IPG_TRIGGER)) {
      final Matcher urlMatcher = Pattern.compile(GATHER_URL_REGEX_PATTERN)
          .matcher(aUrlElement.attr(ATTRIBUTE_DATA_CTRL_IPG_TRIGGER));
      if (urlMatcher.find()) {
        return Optional.of(KikaConstants.BASE_URL + urlMatcher.group());
      }
    }
    return Optional.empty();
  }

  private Set<String> getAllowedDateStrings() {
    final Set<String> dateStrings = new HashSet<>();
    for (int i = 0; i < crawler.getCrawlerConfig().getMaximumDaysForSendungVerpasstSection(); i++) {
      dateStrings
          .add(LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    for (int i = 1; i <= crawler.getCrawlerConfig().getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      dateStrings
          .add(LocalDateTime.now().plus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER));
    }

    return dateStrings;
  }

}
