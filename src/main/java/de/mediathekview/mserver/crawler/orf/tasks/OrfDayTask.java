package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;

public class OrfDayTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String ITEM_SELECTOR = "article a";
  private static final String TITLE_SELECTOR1 = ".item-title";
  private static final String TITLE_SELECTOR2 = ".teaser-title";

  public OrfDayTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs, jsoupConnection);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO urlDto, final Document document) {
    final Elements elements = document.select(ITEM_SELECTOR);
    elements.forEach(
        item -> {
          final Element titleElement = getTitleElement(item);
          if (titleElement != null) {
            final String theme = OrfHelper.parseTheme(titleElement.text());
            final String url = item.attr(HtmlConsts.ATTRIBUTE_HREF);

            final TopicUrlDTO dto = new TopicUrlDTO(theme, url);
            taskResults.add(dto);
          }
        });
  }

  private Element getTitleElement(final Element item) {
    Element titleElement = item.selectFirst(TITLE_SELECTOR1);
    if (titleElement == null) {
      titleElement = item.selectFirst(TITLE_SELECTOR2);
    }
    return titleElement;
  }

  @Override
  protected AbstractUrlTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new OrfDayTask(crawler, aUrlsToCrawl, getJsoupConnection());
  }
}
