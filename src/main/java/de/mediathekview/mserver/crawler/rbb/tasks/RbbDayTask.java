package de.mediathekview.mserver.crawler.rbb.tasks;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class RbbDayTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_ENTRY = "div.box > div.entries > div.entry";
  private static final String SELECTOR_URL = "a.mediaLink";

  public RbbDayTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {
    final Elements entries = aDocument.select(SELECTOR_ENTRY);
    entries.forEach(entry -> {
      final Set<String> urls = parseUrls(entry);
      urls.forEach(url -> taskResults.add(new CrawlerUrlDTO(url)));
    });
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbDayTask(crawler, aElementsToProcess);
  }

  private Set<String> parseUrls(final Element aEntry) {
    Set<String> urls = new HashSet<>();

    final Elements urlElements = aEntry.select(SELECTOR_URL);
    urlElements.forEach(urlElement -> {
      String url = urlElement.attr(ATTRIBUTE_HREF);
      url = UrlUtils.addDomainIfMissing(url, RbbConstants.URL_BASE);
      urls.add(url);
    });

    return urls;
  }
}
