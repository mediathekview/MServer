package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KikaLetterPageUrlTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.bundleNaviItem > a.pageItem";
  private final String baseUrl;

  public KikaLetterPageUrlTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDtos);
    this.baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {
    final Elements urlElements = aDocument.select(LETTER_PAGE_URL_SELECTOR);
    for (Element urlElement : urlElements) {
      final String url = urlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageUrlTask(crawler, aElementsToProcess, baseUrl);
  }
}
