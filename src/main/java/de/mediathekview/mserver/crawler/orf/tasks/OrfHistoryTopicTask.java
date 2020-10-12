package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import org.jsoup.nodes.Document;

import java.util.Queue;

public class OrfHistoryTopicTask extends AbstractDocumentTask<TopicUrlDTO, TopicUrlDTO> {

  private static final String SHOW_URL_SELECTOR = "article > a";

  public OrfHistoryTopicTask(
      final AbstractCrawler crawler,
      final Queue<TopicUrlDTO> urlToCrawlDTOs,
      final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs, jsoupConnection);
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
      final Queue<TopicUrlDTO> aElementsToProcess) {
    return new OrfHistoryTopicTask(crawler, aElementsToProcess, getJsoupConnection());
  }

  @Override
  protected void processDocument(final TopicUrlDTO aUrlDto, final Document aDocument) {
    aDocument
        .select(SHOW_URL_SELECTOR)
        .forEach(
            showElement -> {
              final String url = showElement.attr(HtmlConsts.ATTRIBUTE_HREF);
              taskResults.add(new TopicUrlDTO(aUrlDto.getTopic(), url));
            });
  }
}
