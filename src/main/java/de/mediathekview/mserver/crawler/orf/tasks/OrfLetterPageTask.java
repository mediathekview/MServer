package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OrfLetterPageTask implements Callable<Queue<TopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(OrfLetterPageTask.class);

  private static final String SHOW_URL_SELECTOR = "article > a";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public OrfLetterPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Queue<TopicUrlDTO> call() throws Exception {
    final Queue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document = crawler.getConnection().requestBodyAsHtmlDocument(OrfConstants.URL_SHOW_LETTER_PAGE_A);
    final List<String> overviewLinks = OrfHelper.parseLetterLinks(document);

    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(
        url -> {
          try {
            final Document subpageDocument = crawler.requestBodyAsHtmlDocument(url);
            results.addAll(parseOverviewPage(subpageDocument));
          } catch (final IOException ex) {
            LOG.fatal("OrfLetterPageTask: error parsing url {}", url, ex);
          } catch (final NullPointerException e) {
            LOG.fatal(e);
          }
        });

    return results;
  }

  private Queue<TopicUrlDTO> parseOverviewPage(final Document aDocument) {
    final Queue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    final Elements links = aDocument.select(SHOW_URL_SELECTOR);
    links.forEach(
        element -> {
          if (element.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
            final String link = element.attr(HtmlConsts.ATTRIBUTE_HREF);
            final String theme = OrfHelper.parseTheme(element);

            results.add(new TopicUrlDTO(theme, link));
          }
        });

    return results;
  }
}
