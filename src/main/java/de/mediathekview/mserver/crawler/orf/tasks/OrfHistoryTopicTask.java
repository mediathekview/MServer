package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class OrfHistoryTopicTask extends AbstractDocumentTask<TopicUrlDTO, TopicUrlDTO> {

  private static final String SHOW_URL_SELECTOR = "article > a";

  public OrfHistoryTopicTask(
      AbstractCrawler aCrawler, ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractRecrusivConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new OrfHistoryTopicTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDTO, Document aDocument) {
    aDocument
        .select(SHOW_URL_SELECTOR)
        .forEach(
            showElement -> {
              final String url = showElement.attr(Consts.ATTRIBUTE_HREF);
              taskResults.add(new TopicUrlDTO(aUrlDTO.getTopic(), url));
            });
  }
}
