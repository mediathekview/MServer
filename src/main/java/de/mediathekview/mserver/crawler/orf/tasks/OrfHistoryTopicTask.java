package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import org.jsoup.nodes.Document;

import java.util.concurrent.ConcurrentLinkedQueue;

public class OrfHistoryTopicTask extends AbstractDocumentTask<TopicUrlDTO, TopicUrlDTO> {

  private static final String SHOW_URL_SELECTOR = "article > a";

  public OrfHistoryTopicTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected AbstractRecrusivConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new OrfHistoryTopicTask(crawler, aElementsToProcess);
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
