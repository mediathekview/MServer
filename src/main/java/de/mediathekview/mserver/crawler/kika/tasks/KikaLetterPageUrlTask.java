package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaLetterPageUrlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.bundleNaviItem > a.pageItem";
  private final String baseUrl;

  public KikaLetterPageUrlTask(
          final AbstractCrawler aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final String aBaseUrl,
          final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
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
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageUrlTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
