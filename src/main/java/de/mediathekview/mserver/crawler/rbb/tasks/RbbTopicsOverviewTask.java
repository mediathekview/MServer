package de.mediathekview.mserver.crawler.rbb.tasks;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RbbTopicsOverviewTask extends AbstractDocumentTask<TopicUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC_ENTRY = "div.elementWrapper div.teaser > div.textWrapper > a.textLink";
  private static final String SELECTOR_TOPIC_NAME = "h4.headline";

  public RbbTopicsOverviewTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {
    final Elements topics = aDocument.select(SELECTOR_TOPIC_ENTRY);
    topics.forEach(topic -> {
      final Element topicName = topic.select(SELECTOR_TOPIC_NAME).first();
      final String url = topic.attr(ATTRIBUTE_HREF);
      taskResults.add(new TopicUrlDTO(topicName.text(), UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE)));
    });
  }

  @Override
  protected AbstractRecrusivConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbTopicsOverviewTask(crawler, aElementsToProcess);
  }
}
