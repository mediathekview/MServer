package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.HrConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

public class HrSendungenOverviewPageTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final String SENDUNGSFOLEN_OVERVIEW_URL_REPLACEMENT = "sendungen/index.html";
  private static final String HESSENSCHAU_OVERVIEW_URL_REPLACEMENT = "sendungsarchiv/index.html";
  private static final String INDEX_PAGE_NAME = "index.html";
  private static final Logger LOG = LogManager.getLogger(HrSendungenOverviewPageTask.class);
  private static final String HR_SENDUNGEN_URL = "sendungen-a-z/index.html";
  private static final String SENDUNG_URL_SELECTOR = ".c-teaser__headlineLink.link";

  private final String baseUrl;
  private final AbstractCrawler crawler;

  public HrSendungenOverviewPageTask(final String aBaseUrl, final AbstractCrawler aCrawler) {
    baseUrl = aBaseUrl;
    crawler = aCrawler;
  }

  @Override
  public Set<CrawlerUrlDTO> call() {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    try {
      final Document document = crawler.requestBodyAsHtmlDocument(baseUrl + HR_SENDUNGEN_URL);
      for (final Element filmUrlElement : document.select(SENDUNG_URL_SELECTOR)) {
        if (filmUrlElement.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
          final String url = filmUrlElement.absUrl(HtmlConsts.ATTRIBUTE_HREF);
          if (isUrlRelevant(url)) {
            results.add(new CrawlerUrlDTO(prepareUrl(url)));
          }
        }
      }
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something went terrible wrong on gathering the HR Sendung URLs from Sendungen overview page.",
          ioException);
    }

    return results;
  }

  private String prepareUrl(final String aUrl) {
    if (aUrl.contains(HrConstants.BASE_URL_HESSENSCHAU)) {
      return aUrl.replace(INDEX_PAGE_NAME, HESSENSCHAU_OVERVIEW_URL_REPLACEMENT);
    }

    final String preparedUrl =
        aUrl.replace(INDEX_PAGE_NAME, SENDUNGSFOLEN_OVERVIEW_URL_REPLACEMENT);
    if (crawler.requestUrlExists(preparedUrl)) {
      return preparedUrl;
    }

    return aUrl;
  }

  /**
   * filters urls of other ARD stations.
   *
   * @param aUrl the url to check
   * @return true if the url is a HR url else false
   */
  protected boolean isUrlRelevant(final String aUrl) {
    return aUrl.contains(baseUrl) || aUrl.contains(HrConstants.BASE_URL_HESSENSCHAU);
  }
}
