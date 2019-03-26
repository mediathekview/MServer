package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfLetterPageTask implements Callable<ConcurrentLinkedQueue<TopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfLetterPageTask.class);

  private static final String SHOW_URL_SELECTOR = "article > a";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public OrfLetterPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ConcurrentLinkedQueue<TopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document =
        Jsoup.connect(OrfConstants.URL_SHOW_LETTER_PAGE_A)
            .timeout(
                (int)
                    TimeUnit.SECONDS.toMillis(
                        crawler.getCrawlerConfig().getSocketTimeoutInSeconds()))
            .get();
    final List<String> overviewLinks = OrfHelper.parseLetterLinks(document);

    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(
        url -> {
          try {
            final Document subpageDocument =
                Jsoup.connect(url)
                    .timeout(
                        (int)
                            TimeUnit.SECONDS.toMillis(
                                crawler.getCrawlerConfig().getSocketTimeoutInSeconds()))
                    .get();
            results.addAll(parseOverviewPage(subpageDocument));
          } catch (final IOException ex) {
            LOG.fatal("OrfLetterPageTask: error parsing url " + url, ex);
          } catch (NullPointerException e) {
            LOG.fatal(e);
          }
        });

    return results;
  }

  private ConcurrentLinkedQueue<TopicUrlDTO> parseOverviewPage(final Document aDocument) {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    final Elements links = aDocument.select(SHOW_URL_SELECTOR);
    links.forEach(
        element -> {
          if (element.hasAttr(Consts.ATTRIBUTE_HREF)) {
            final String link = element.attr(Consts.ATTRIBUTE_HREF);
            final String theme = OrfHelper.parseTheme(element);

            results.add(new TopicUrlDTO(theme, link));
          }
        });

    return results;
  }
}
