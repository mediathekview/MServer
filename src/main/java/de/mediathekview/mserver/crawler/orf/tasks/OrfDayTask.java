package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfDayTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article.item > a";
  
  public OrfDayTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }
  
  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(item -> {
      String theme = OrfHelper.parseTheme(item);
      String url = item.attr(Consts.ATTRIBUTE_HREF);
      
      TopicUrlDTO dto = new TopicUrlDTO(theme, url);
      taskResults.add(dto);
    });
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new OrfDayTask(crawler, aURLsToCrawl);
  }
}
