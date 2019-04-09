package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KikaTopicOverviewPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC_OVERVIEW = "div.boxBroadcast a.linkAll";
  private static final String SELECTOR_SUBPAGES = ".modBundleGroupNavi:eq(1) div.bundleNaviItem > a.pageItem";
  
  private final String baseUrl;
  private final int pageNumber;

  public KikaTopicOverviewPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, String aBaseUrl) {
    this(aCrawler, aUrlToCrawlDTOs, aBaseUrl, 1);
  }
  
  public KikaTopicOverviewPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, String aBaseUrl, int pageNumber) {
    super(aCrawler, aUrlToCrawlDTOs);
    this.baseUrl = aBaseUrl;
    this.pageNumber = pageNumber;
  }
  
  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    parseFilmUrls(aDocument);
    
    final Optional<CrawlerUrlDTO> nextPageLink = parseNextPageUrl(aDocument);
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > pageNumber) {
      loadNextPage(nextPageLink.get());
    }
  }

  private void loadNextPage(final CrawlerUrlDTO nextPageLink) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.add(nextPageLink);
    AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> subPageCrawler = createNewOwnInstance(nextPageLinks);
    subPageCrawler.fork();
    Set<CrawlerUrlDTO> join = subPageCrawler.join();
    taskResults.addAll(join);
  }

  private void parseFilmUrls(Document aDocument) {
    Elements urlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW);
    for(Element urlElement : urlElements) {
      final String url = urlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaTopicOverviewPageTask(crawler, aElementsToProcess, baseUrl, pageNumber + 1);
  }

  private Optional<CrawlerUrlDTO> parseNextPageUrl(Document aDocument) {
    Elements subpageElements = aDocument.select(SELECTOR_SUBPAGES);
    if (subpageElements.size() > pageNumber) {
      final String url = subpageElements.get(pageNumber).attr(Consts.ATTRIBUTE_HREF);
      return Optional.of(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
    
    return Optional.empty();
  }
  
}
