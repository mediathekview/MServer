package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopicPageDeserializer;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class MdrTopicPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrTopicPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    MdrTopicPageDeserializer topicPageDeserializer = new MdrTopicPageDeserializer(MdrConstants.URL_BASE);
    MdrTopic topic = topicPageDeserializer.deserialize(aDocument);

    taskResults.addAll(topic.getFilmUrls());

    if (topic.getNextPage().isPresent()) {
      processNextPage(topic.getNextPage().get());
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrTopicPageTask(crawler, aElementsToProcess);
  }

  private void processNextPage(CrawlerUrlDTO aCrawlerUrlDTO) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(aCrawlerUrlDTO);

    taskResults.addAll(createNewOwnInstance(urls).invoke());
  }
}
