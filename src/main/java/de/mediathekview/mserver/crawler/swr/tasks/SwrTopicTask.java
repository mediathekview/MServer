package de.mediathekview.mserver.crawler.swr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import de.mediathekview.mserver.crawler.swr.parser.SwrTopicPageDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class SwrTopicTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final int pageNumber;

  public SwrTopicTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    pageNumber = 1;
  }

  public SwrTopicTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess, int aPageNumber) {
    super(aCrawler, aElementsToProcess);

    pageNumber = aPageNumber;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    SwrTopicPageDeserializer topicPageDeserializer = new SwrTopicPageDeserializer(SwrConstants.URL_BASE);
    MdrTopic topic = topicPageDeserializer.deserialize(aDocument);

    taskResults.addAll(topic.getFilmUrls());

    if (topic.getNextPage().isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
      processNextPage(topic.getNextPage().get());
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new SwrTopicTask(crawler, aElementsToProcess);
  }

  private void processNextPage(CrawlerUrlDTO aCrawlerUrlDTO) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(aCrawlerUrlDTO);

    taskResults.addAll(new SwrTopicTask(crawler, urls, pageNumber + 1).invoke());
  }
}
