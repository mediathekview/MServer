package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaTopicOverviewPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  // siehe PUR+, es gibt nicht immer einen boxBroadcast
  private static final String SELECTOR_TOPIC_OVERVIEW = "a.linkAll";
  private static final String SELECTOR_SUBPAGES =
      ".modBundleGroupNavi:eq(1) div.bundleNaviItem > a.pageItem";
  private static final String SELECTOR_TYPE_ICON = "span.icon-font";
  private static final String ENTRY_ICON_FILM = "";

  private final String baseUrl;
  private final int pageNumber;

  public KikaTopicOverviewPageTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    this(aCrawler, aUrlToCrawlDtos, aBaseUrl, jsoupConnection, 1);
  }

  private KikaTopicOverviewPageTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection,
      final int pageNumber) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
    baseUrl = aBaseUrl;
    this.pageNumber = pageNumber;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    parseFilmUrls(aDocument);

    if (pageNumber == 1) {
      final List<CrawlerUrlDTO> nextPageUrls =
          sortNextPageUrls(parseNextPageUrls(aDocument), aUrlDto);

      if (!nextPageUrls.isEmpty()) {
        final int maxSubPage =
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
      final List<CrawlerUrlDTO> nextPageUrls, final CrawlerUrlDTO actualUrl) {

    if (nextPageUrls.isEmpty()) {
      return nextPageUrls;
    }

    final int actualIndex = nextPageUrls.indexOf(actualUrl);
    if (actualIndex < 0) {
      // wenn Url nicht enthalten ist, dann ist auf die erste Seite verlinkt und keine Sortierung
      // nötig
      return nextPageUrls;
    }

    final List<CrawlerUrlDTO> sortedUrls = new ArrayList<>();

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

    final Queue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.addAll(nextPageUrls);
    final AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> subPageCrawler =
        createNewOwnInstance(nextPageLinks, pageNumber + 1);
    subPageCrawler.fork();
    final Set<CrawlerUrlDTO> join = subPageCrawler.join();
    taskResults.addAll(join);
  }

  private void parseFilmUrls(final Document aDocument) {
    final Elements urlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW);
    for (final Element urlElement : urlElements) {
      final String url = urlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      final Element iconElement = urlElement.parent().select(SELECTOR_TYPE_ICON).first();
      if (iconElement != null && iconElement.text().equals(ENTRY_ICON_FILM)) {
        taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
      }
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return createNewOwnInstance(aElementsToProcess, 1);
  }

  private AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess, final int aPageNumber) {
    return new KikaTopicOverviewPageTask(
        crawler, aElementsToProcess, baseUrl, getJsoupConnection(), aPageNumber);
  }

  private List<CrawlerUrlDTO> parseNextPageUrls(final Document aDocument) {
    final List<CrawlerUrlDTO> nextPages = new ArrayList<>();

    final Elements subPageElements = aDocument.select(SELECTOR_SUBPAGES);
    for (final Element subPageElement : subPageElements) {
      final String url = subPageElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      nextPages.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
    }

    return nextPages;
  }
}
