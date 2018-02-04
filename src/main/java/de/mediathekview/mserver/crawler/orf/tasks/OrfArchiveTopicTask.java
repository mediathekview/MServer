package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfArchiveTopicTask extends AbstractDocumentTask<OrfTopicUrlDTO, OrfTopicUrlDTO> {

  private static final String ITEM_SELECTOR = "article.item > a";
  
  public OrfArchiveTopicTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<OrfTopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }
  
  @Override
  protected void processDocument(OrfTopicUrlDTO aUrlDTO, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(item -> {
      String url = item.attr(Consts.ATTRIBUTE_HREF);
      
      OrfTopicUrlDTO dto = new OrfTopicUrlDTO(aUrlDTO.getTheme(), url);
      taskResults.add(dto);
    });
  }

  @Override
  protected AbstractUrlTask<OrfTopicUrlDTO, OrfTopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<OrfTopicUrlDTO> aURLsToCrawl) {
    return new OrfArchiveTopicTask(crawler, aURLsToCrawl);
  }
}
