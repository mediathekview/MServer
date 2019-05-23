package de.mediathekview.mserver.crawler.rbb.tasks;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class RbbTopicOverviewTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String ATTRIBUTE_DISABLED = "aria-disabled";

  private static final String SELECTOR_NEXT_PAGE =
      "div.entries > div.entry > a[aria-labelledby=cursorRight]";
  private static final String SELECTOR_TOPIC_ENTRY =
      "div.elementWrapper div.teaser > div.textWrapper > a.textLink";

  private final int pageNumber;

  public RbbTopicOverviewTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
    pageNumber = 1;
  }

  public RbbTopicOverviewTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDtos);
    pageNumber = aPageNumber;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    final Elements topics = aDocument.select(SELECTOR_TOPIC_ENTRY);
    topics.forEach(
        topic -> {
          final String url = topic.attr(ATTRIBUTE_HREF);
          taskResults.add(
              new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE)));
        });

    if (pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
      processNextPage(aDocument);
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbTopicOverviewTask(crawler, aElementsToProcess);
  }

  private void processNextPage(final Document aDocument) {
    final Elements nextPages = aDocument.select(SELECTOR_NEXT_PAGE);
    if (nextPages.size() == 1) {
      final Element nextPage = nextPages.first();

      // if attribute aria-disabled exists, no next page exists
      if (!nextPage.hasAttr(ATTRIBUTE_DISABLED)) {
        String url = nextPage.attr(ATTRIBUTE_HREF);
        url = UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE);

        final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
        urls.add(new CrawlerUrlDTO(url));

        taskResults.addAll(new RbbTopicOverviewTask(crawler, urls, pageNumber + 1).invoke());
      }
    }
  }
}
