package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KikaTopicOverviewPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final String SELECTOR_TOPIC_OVERVIEW = "div.boxBroadcast a.linkAll";
  private static final String SELECTOR_SUBPAGES =
      ".modBundleGroupNavi:eq(1) div.bundleNaviItem > a.pageItem";

  private final String baseUrl;
  private final int pageNumber;

  public KikaTopicOverviewPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      String aBaseUrl) {
    this(aCrawler, aUrlToCrawlDtos, aBaseUrl, 1);
  }

  private KikaTopicOverviewPageTask(
      AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      String aBaseUrl,
      int pageNumber) {
    super(aCrawler, aUrlToCrawlDtos);
    this.baseUrl = aBaseUrl;
    this.pageNumber = pageNumber;
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {
    parseFilmUrls(aDocument);

    if (pageNumber == 1) {
      List<CrawlerUrlDTO> nextPageUrls =
          sortNextPageUrls(parseNextPageUrls(aDocument), aUrlDto);

      if (!nextPageUrls.isEmpty()) {
        int maxSubPage =
            config.getMaximumSubpages() > nextPageUrls.size()
                ? nextPageUrls.size()
                : config.getMaximumSubpages();
        loadNextPage(nextPageUrls.subList(1, maxSubPage));
      }
    }
  }

  /**
   * Manchmal verlinkt die Folgenübersicht nicht auf die erste Seite. Deshalb muss die Übersicht so
   * sortiert werden, dass die verlinkte Seite, die erste ist und die anschließende Filterung
   * korrekt funktioniert.
   *
   * @param nextPageUrls Urls für Übersichtsseiten
   * @param actualUrl Url der aktuellen Seite
   * @return sortierte Liste der Übersichtsseiten
   */
  private List<CrawlerUrlDTO> sortNextPageUrls(
      List<CrawlerUrlDTO> nextPageUrls, final CrawlerUrlDTO actualUrl) {

    if (nextPageUrls.isEmpty()) {
      return nextPageUrls;
    }

    int actualIndex = nextPageUrls.indexOf(actualUrl);
    if (actualIndex < 0) {
      // wenn Url nicht enthalten ist, dann ist auf die erste Seite verlinkt und keine Sortierung nötig
      return nextPageUrls;
    }

    List<CrawlerUrlDTO> sortedUrls = new ArrayList<>();

    for (int i = actualIndex; i < nextPageUrls.size(); i++) {
      sortedUrls.add(nextPageUrls.get(i));
    }
    for (int i = 0; i < actualIndex; i++) {
      sortedUrls.add(nextPageUrls.get(i));
    }

    return sortedUrls;
  }

  private void loadNextPage(final List<CrawlerUrlDTO> nextPageUrls) {
    if (nextPageUrls.isEmpty()) {
      return;
    }

    final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.addAll(nextPageUrls);
    AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> subPageCrawler =
        createNewOwnInstance(nextPageLinks);
    subPageCrawler.fork();
    Set<CrawlerUrlDTO> join = subPageCrawler.join();
    taskResults.addAll(join);
  }

  private void parseFilmUrls(Document aDocument) {
    Elements urlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW);
    for (Element urlElement : urlElements) {
      final String url = urlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaTopicOverviewPageTask(crawler, aElementsToProcess, baseUrl, pageNumber + 1);
  }

  private List<CrawlerUrlDTO> parseNextPageUrls(Document aDocument) {
    List<CrawlerUrlDTO> nextPages = new ArrayList<>();

    Elements subPageElements = aDocument.select(SELECTOR_SUBPAGES);
    for (Element subPageElement : subPageElements) {
      final String url = subPageElement.attr(Consts.ATTRIBUTE_HREF);
      nextPages.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }

    return nextPages;
  }
}
