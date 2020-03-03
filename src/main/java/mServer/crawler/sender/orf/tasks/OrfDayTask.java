package mServer.crawler.sender.orf.tasks;

import mServer.crawler.sender.base.AbstractUrlTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OrfDayTask extends OrfTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article a";
  private static final String TITLE_SELECTOR1 = ".item-title";
  private static final String TITLE_SELECTOR2 = ".teaser-title";
  private static final String ATTRIBUTE_HREF = "href";

  public OrfDayTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(
            item -> {
              Element titleElement = getTitleElement(item);
              if (titleElement != null) {
                String theme = OrfHelper.parseTheme(titleElement.text());
                String url = item.attr(ATTRIBUTE_HREF);

                TopicUrlDTO dto = new TopicUrlDTO(theme, url);
                taskResults.add(dto);
              }
            });

    ORF_LOGGER.trace(String.format("%s: Anzahl Filme: %d", aUrlDTO.getUrl(), taskResults.size()));
  }

  private Element getTitleElement(Element item) {
    Element titleElement = item.selectFirst(TITLE_SELECTOR1);
    if (titleElement == null) {
      titleElement = item.selectFirst(TITLE_SELECTOR2);
    }
    return titleElement;
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new OrfDayTask(crawler, aURLsToCrawl);
  }
}
