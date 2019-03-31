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

public class KikaLetterPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String TOPIC_URL_SELECTOR = ".teaserBroadcastSeries .linkAll";
  private final String baseUrl;
  
  public KikaLetterPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDTOs);
    this.baseUrl = aBaseUrl;
  }
  
  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    final Elements topicUrlElements = aDocument.select(TOPIC_URL_SELECTOR);
    for(Element topicUrlElement : topicUrlElements) {
      final String url = topicUrlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageTask(crawler, aElementsToProcess, baseUrl);
  }
  
}
