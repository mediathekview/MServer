package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class OrfDayTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article a";
  private static final String TITLE_SELECTOR1 = ".item-title";
  private static final String TITLE_SELECTOR2 = ".teaser-title";

  public OrfDayTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {
    Elements elements = aDocument.select(ITEM_SELECTOR);
    elements.forEach(
        item -> {
          Element titleElement = getTitleElement(item);
          if (titleElement != null) {
            String theme = OrfHelper.parseTheme(titleElement.text());
            String url = item.attr(Consts.ATTRIBUTE_HREF);

            TopicUrlDTO dto = new TopicUrlDTO(theme, url);
            taskResults.add(dto);
          }
        });
  }

  private Element getTitleElement(Element item) {
    Element titleElement = item.selectFirst(TITLE_SELECTOR1);
    if (titleElement == null) {
      titleElement = item.selectFirst(TITLE_SELECTOR2);
    }
    return titleElement;
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new OrfDayTask(crawler, aUrlsToCrawl);
  }
}
