package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlParseException;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SrTopicsOverviewPageTask implements Callable<ConcurrentLinkedQueue<SrTopicUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrTopicsOverviewPageTask.class);

  private static final String URL_PARAMETER_SUBPAGE = "a_z";
  private static final String URL_PARAMETER_SHOW_SHORTNAME = "sen";

  private static final String SHOW_PAGE_URL_SELECTOR = "div.container > div.row > a";
  private static final String SHOW_LINK_SELECTOR = "h3.teaser__text__header a";
  private final AbstractCrawler crawler;

  JsoupConnection jsoupConnection;

  /** @param aCrawler The crawler which uses this task. */
  public SrTopicsOverviewPageTask(final AbstractCrawler aCrawler, JsoupConnection jsoupConnection) {
    crawler = aCrawler;
    this.jsoupConnection = jsoupConnection;
  }

  private static SrTopicUrlDTO createDto(final String aTheme, final String aShowShort) {
    final String url = String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, aShowShort, 1);
    return new SrTopicUrlDTO(aTheme, url);
  }

  @Override
  public ConcurrentLinkedQueue<SrTopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<SrTopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document =
        jsoupConnection.getDocumentTimeoutAfter(SrConstants.URL_OVERVIEW_PAGE,
            (int) TimeUnit.SECONDS.toMillis(
                crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
    final List<String> overviewLinks = parseOverviewLinks(document);

    // Sendungen für erste Seite ermitteln
    results.addAll(parseOverviewPage(document));

    // Sendungen für weitere Seiten ermitteln
    overviewLinks.forEach(
        url -> {
          try {
            final Document subpageDocument = jsoupConnection.getDocumentTimeoutAfter(url,
                (int) TimeUnit.SECONDS
                    .toMillis(crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
            results.addAll(parseOverviewPage(subpageDocument));
          } catch (final IOException ex) {
            LOG.fatal("SrTopicsOverviewPageTask: error parsing url " + url, ex);
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
          try {
            final Optional<String> subpage =
                UrlUtils.getUrlParameterValue(
                    element.attr(HtmlConsts.ATTRIBUTE_HREF), URL_PARAMETER_SUBPAGE);
            if (subpage.isPresent()) {
              results.add(SrConstants.URL_OVERVIEW_PAGE + subpage.get());
            }
          } catch (final UrlParseException ex) {
            LOG.fatal(ex);
          }
        });

    return results;
  }

  private ConcurrentLinkedQueue<SrTopicUrlDTO> parseOverviewPage(final Document aDocument) {
    final ConcurrentLinkedQueue<SrTopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    final Elements links = aDocument.select(SHOW_LINK_SELECTOR);
    links.forEach(
        element -> {
          try {
            final Optional<String> showShort =
                UrlUtils.getUrlParameterValue(
                    element.attr(HtmlConsts.ATTRIBUTE_HREF), URL_PARAMETER_SHOW_SHORTNAME);
            if (showShort.isPresent()) {
              results.add(createDto(element.text(), showShort.get()));
            }
          } catch (final UrlParseException ex) {
            LOG.fatal(ex);
          }
        });

    return results;
  }
}
