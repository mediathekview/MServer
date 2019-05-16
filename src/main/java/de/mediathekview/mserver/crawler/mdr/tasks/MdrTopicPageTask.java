package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopicPageDeserializer;
import org.jsoup.nodes.Document;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;

public class MdrTopicPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrTopicPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    final MdrTopicPageDeserializer topicPageDeserializer =
        new MdrTopicPageDeserializer(MdrConstants.URL_BASE);
    final MdrTopic topic = topicPageDeserializer.deserialize(aDocument);

    final Optional<ForkJoinTask<Set<CrawlerUrlDTO>>> subpageCrawler;
    final Optional<CrawlerUrlDTO> nextPageUrl = topic.getNextPage();
    subpageCrawler =
        nextPageUrl.map(
            crawlerUrlDTO ->
                createNewOwnInstance(
                        new ConcurrentLinkedQueue<>(Collections.singleton(crawlerUrlDTO)))
                    .fork());

    final Set<CrawlerUrlDTO> filmUrls = topic.getFilmUrls();
    taskResults.addAll(filmUrls);
    crawler.incrementMaxCountBySizeAndGetNewSize(filmUrls.size());
    crawler.updateProgress();

    subpageCrawler.ifPresent(subpage -> taskResults.addAll(subpage.join()));
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrTopicPageTask(crawler, aElementsToProcess);
  }
}
