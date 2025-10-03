package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SrTopicsOverviewPageTask implements Callable<Queue<SrTopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrTopicsOverviewPageTask.class);

  private static final String URL_PARAMETER_SUBPAGE = "a_z";
  private static final String URL_PARAMETER_SHOW_SHORTNAME = "sen";

  private static final String SHOW_PAGE_URL_SELECTOR = "div.container > div.row > a";
  private static final String SHOW_LINK_SELECTOR = "h3.teaser__text__header a";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public SrTopicsOverviewPageTask(
      final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  private static SrTopicUrlDTO createDto(final String aTheme, final String aShowShort) {
    final String url = String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, aShowShort, 1);
    return new SrTopicUrlDTO(aTheme, url);
  }

  @Override
  public Queue<SrTopicUrlDTO> call() throws Exception {
    final Queue<SrTopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document = crawler.requestBodyAsHtmlDocument(SrConstants.URL_OVERVIEW_PAGE);
    final List<String> overviewLinks = parseOverviewLinks(document);

    // Sendungen für erste Seite ermitteln
    results.addAll(parseOverviewPage(document));

    // Sendungen für weitere Seiten ermitteln
    overviewLinks.forEach(
        url -> {
          try {
            final Document subpageDocument = crawler.requestBodyAsHtmlDocument(url);
            results.addAll(parseOverviewPage(subpageDocument));
          } catch (final IOException ex) {
            LOG.fatal("SrTopicsOverviewPageTask: error parsing url {}", url, ex);
          }
        });

    return results;
  }

  /**
   * Ermittelt aus der HTML-Seite die Links für die weiteren Übersichtsseiten
   *
   * @param aDocument das Dokument der ersten Übersichtsseite
   * @return Liste der URLs der weiteren Übersichtsseiten
   */
  private List<String> parseOverviewLinks(final Document aDocument) {
    final List<String> results = new ArrayList<>();

    final Elements links = aDocument.select(SHOW_PAGE_URL_SELECTOR);
    links.forEach(
        element -> {
            final Optional<String> subpage =
                UrlUtils.getUrlParameterValue(
                    element.attr(HtmlConsts.ATTRIBUTE_HREF), URL_PARAMETER_SUBPAGE);
            if (subpage.isPresent()) {
              results.add(SrConstants.URL_OVERVIEW_PAGE + subpage.get());
            }
        });

    return results;
  }

  private Queue<SrTopicUrlDTO> parseOverviewPage(final Document aDocument) {
    final Queue<SrTopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    final Elements links = aDocument.select(SHOW_LINK_SELECTOR);
    links.forEach(
        element -> {
            final Optional<String> showShort =
                UrlUtils.getUrlParameterValue(
                    element.attr(HtmlConsts.ATTRIBUTE_HREF), URL_PARAMETER_SHOW_SHORTNAME);
            if (showShort.isPresent()) {
              results.add(createDto(element.text(), showShort.get()));
            }
        });

    return results;
  }
}
