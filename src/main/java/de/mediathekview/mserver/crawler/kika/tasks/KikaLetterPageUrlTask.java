package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;

public class KikaLetterPageUrlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.bundleNaviItem > a.pageItem";
  private final String baseUrl;

  public KikaLetterPageUrlTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, urlToCrawlDTOs, jsoupConnection);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    final Elements urlElements = aDocument.select(LETTER_PAGE_URL_SELECTOR);
    for (final Element urlElement : urlElements) {
      final String url = urlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageUrlTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
