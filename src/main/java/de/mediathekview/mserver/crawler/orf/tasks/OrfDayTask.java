package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OrfDayTask extends AbstractDocumentTask<OrfTopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article.item > a";
  
  public OrfDayTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }
  
  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(item -> {
      String theme = parseTheme(item);
      String url = item.attr(Consts.ATTRIBUTE_HREF);
      
      OrfTopicUrlDTO dto = new OrfTopicUrlDTO(theme, url);
      taskResults.add(dto);
    });
  }

  @Override
  protected AbstractUrlTask<OrfTopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new OrfDayTask(crawler, aURLsToCrawl);
  }
  
  private static String parseTheme(final Element aItem) {
    String theme = aItem.attr(Consts.ATTRIBUTE_TITLE);
    
    // Thema steht vor Doppelpunkt, Ausnahme ZIB-Sendungen mit Uhrzeit
    int index = theme.indexOf(":");
    if (index > 0 && theme.charAt(index+1) != '0') {
      return theme.substring(0, index);
    }
    
    return theme;
  }  
}
