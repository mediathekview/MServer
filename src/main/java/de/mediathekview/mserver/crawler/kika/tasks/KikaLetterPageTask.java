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

public class KikaLetterPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String TOPIC_URL_SELECTOR = ".teaserBroadcastSeries .linkAll";
  private final String baseUrl;

  public KikaLetterPageTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDTOs, jsoupConnection);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Elements topicUrlElements = aDocument.select(TOPIC_URL_SELECTOR);
    for (final Element topicUrlElement : topicUrlElements) {
      final String url = topicUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
