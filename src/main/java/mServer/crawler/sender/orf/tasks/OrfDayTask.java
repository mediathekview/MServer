package mServer.crawler.sender.orf.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.orf.CrawlerUrlDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfDayTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article.item > a";
  private static final String ATTRIBUTE_HREF = "href";

  public OrfDayTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(item -> {
      String theme = OrfHelper.parseTheme(item);
      String url = item.attr(ATTRIBUTE_HREF);

      TopicUrlDTO dto = new TopicUrlDTO(theme, url);
      taskResults.add(dto);
    });
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new OrfDayTask(crawler, aURLsToCrawl);
  }
}
