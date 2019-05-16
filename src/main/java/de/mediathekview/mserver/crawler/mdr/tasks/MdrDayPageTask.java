package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.MdrConstants;
import de.mediathekview.mserver.crawler.mdr.parser.MdrDayPageDeserializer;
import org.jsoup.nodes.Document;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MdrDayPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  public MdrDayPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    final MdrDayPageDeserializer dayPageUrlDeserializer =
        new MdrDayPageDeserializer(MdrConstants.URL_BASE);
    final Set<CrawlerUrlDTO> dayPageUrls = dayPageUrlDeserializer.deserialize(aDocument);
    taskResults.addAll(dayPageUrls);
    crawler.incrementMaxCountBySizeAndGetNewSize(dayPageUrls.size());
    crawler.updateProgress();
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrDayPageTask(crawler, aElementsToProcess);
  }
}
