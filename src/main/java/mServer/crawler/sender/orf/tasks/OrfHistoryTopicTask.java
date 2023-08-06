package mServer.crawler.sender.orf.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.jsoup.nodes.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OrfHistoryTopicTask extends AbstractDocumentTask<TopicUrlDTO, TopicUrlDTO> {

  private static final String ATTRIBUTE_HREF = "href";
  private static final String SHOW_URL_SELECTOR = "article > a";

  public OrfHistoryTopicTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<TopicUrlDTO> urlToCrawlDTOs
  ) {
    super(crawler, urlToCrawlDTOs);
  }

  @Override
  protected AbstractRecursivConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new OrfHistoryTopicTask(crawler, aElementsToProcess);
  }

  @Override
  protected void processDocument(final TopicUrlDTO aUrlDto, final Document aDocument) {
    aDocument
            .select(SHOW_URL_SELECTOR)
            .forEach(
                    showElement -> {
                      final String url = showElement.attr(ATTRIBUTE_HREF);
                      taskResults.add(new TopicUrlDTO(aUrlDto.getTopic(), url));
                    });
  }
}
