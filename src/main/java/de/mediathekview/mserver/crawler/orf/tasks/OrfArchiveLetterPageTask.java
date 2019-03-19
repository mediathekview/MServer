package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class OrfArchiveLetterPageTask implements Callable<ConcurrentLinkedQueue<TopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfArchiveLetterPageTask.class);

  private static final String ITEM_SELECTOR = "article.item > a";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public OrfArchiveLetterPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ConcurrentLinkedQueue<TopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document =
        Jsoup.connect(OrfConstants.URL_ARCHIVE)
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
            LOG.fatal("OrfArchiveLetterPageTask: error parsing url " + url, ex);
          }
        });

    return results;
  }

  private ConcurrentLinkedQueue<TopicUrlDTO> parseOverviewPage(final Document aDocument) {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    final Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(
        item -> {
          final String theme = OrfHelper.parseTheme(item);
          final String url = item.attr(Consts.ATTRIBUTE_HREF);

          final TopicUrlDTO dto = new TopicUrlDTO(theme, url);
          results.add(dto);
        });

    return results;
  }
}
