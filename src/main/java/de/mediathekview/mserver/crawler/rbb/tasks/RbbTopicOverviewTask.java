package de.mediathekview.mserver.crawler.rbb.tasks;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RbbTopicOverviewTask extends AbstractDocumentTask<TopicUrlDTO, TopicUrlDTO> {

  private static final String ATTRIBUTE_DISABLED = "aria-disabled";

  private static final String SELECTOR_NEXT_PAGE = "div.entries > div.entry > a[aria-labelledby=cursorRight]";
  private static final String SELECTOR_TOPIC_ENTRY = "div.elementWrapper div.teaser > div.textWrapper > a.textLink";

  private final int pageNumber;

  public RbbTopicOverviewTask(AbstractCrawler aCrawler,
          ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
    pageNumber = 1;
  }

  public RbbTopicOverviewTask(AbstractCrawler aCrawler,
          ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos,
          final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDtos);
    pageNumber = aPageNumber;
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDto, Document aDocument) {
    final Elements topics = aDocument.select(SELECTOR_TOPIC_ENTRY);
    topics.forEach(topic -> {
      final String url = topic.attr(ATTRIBUTE_HREF);
      taskResults.add(new TopicUrlDTO(aUrlDto.getTopic(), UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE)));

    });

    if (pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
      processNextPage(aDocument, aUrlDto.getTopic());
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new RbbTopicOverviewTask(crawler, aElementsToProcess);
  }

  private void processNextPage(final Document aDocument, final String aTopic) {
    Elements nextPages = aDocument.select(SELECTOR_NEXT_PAGE);
    if (nextPages.size() == 1) {
      Element nextPage = nextPages.first();

      // if attribute aria-disabled exists, no next page exists
      if (!nextPage.hasAttr(ATTRIBUTE_DISABLED)) {
        String url = nextPage.attr(ATTRIBUTE_HREF);
        url = UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE);

        ConcurrentLinkedQueue<TopicUrlDTO> urls = new ConcurrentLinkedQueue<>();
        urls.add(new TopicUrlDTO(aTopic, url));

        taskResults.addAll(new RbbTopicOverviewTask(crawler, urls, pageNumber + 1).invoke());
      }
    }
  }
}
