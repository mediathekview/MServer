package mServer.crawler.sender.kika.tasks;

import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.br.Consts;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto.FilmType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaTopicOverviewPageTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {

  private static final String SELECTOR_TOPIC_OVERVIEW_WITH_BOXBROADCAST = "div.boxBroadcast a.linkAll";
  // siehe PUR+, es gibt nicht immer einen boxBroadcast
  private static final String SELECTOR_TOPIC_OVERVIEW_NO_BOXBROADCAST = "a.linkAll";
  private static final String SELECTOR_SUBPAGES =
      ".modBundleGroupNavi:eq(1) div.bundleNaviItem > a.pageItem";
  private static final String SELECTOR_TYPE_ICON = "span.icon-font";
  private static final String ENTRY_ICON_FILM = "";

  private final String baseUrl;
  private final int pageNumber;

  public KikaTopicOverviewPageTask(
      final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    this(aCrawler, aUrlToCrawlDtos, aBaseUrl, jsoupConnection, 1);
  }

  private KikaTopicOverviewPageTask(
      final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection,
      final int pageNumber) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
    baseUrl = aBaseUrl;
    this.pageNumber = pageNumber;
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    parseFilmUrls(aDocument, aUrlDto.getFilmType());

    if (pageNumber == 1) {
      final List<KikaCrawlerUrlDto> nextPageUrls =
          sortNextPageUrls(parseNextPageUrls(aDocument, aUrlDto.getFilmType()), aUrlDto);

      if (!nextPageUrls.isEmpty()) {
        final int maxSubPage =
            getMaximumSubpages() > nextPageUrls.size()
                ? nextPageUrls.size()
                : getMaximumSubpages();
        loadNextPage(nextPageUrls.subList(1, maxSubPage));
      }
    }
  }

  private int getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 3;
    }
    return 0;
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
  private List<KikaCrawlerUrlDto> sortNextPageUrls(
      final List<KikaCrawlerUrlDto> nextPageUrls, final KikaCrawlerUrlDto actualUrl) {

    if (nextPageUrls.isEmpty()) {
      return nextPageUrls;
    }

    final int actualIndex = nextPageUrls.indexOf(actualUrl);
    if (actualIndex < 0) {
      // wenn Url nicht enthalten ist, dann ist auf die erste Seite verlinkt und keine Sortierung
      // nötig
      return nextPageUrls;
    }

    final List<KikaCrawlerUrlDto> sortedUrls = new ArrayList<>();

    for (int i = actualIndex; i < nextPageUrls.size(); i++) {
      sortedUrls.add(nextPageUrls.get(i));
    }
    for (int i = 0; i < actualIndex; i++) {
      sortedUrls.add(nextPageUrls.get(i));
    }

    return sortedUrls;
  }

  private void loadNextPage(final List<KikaCrawlerUrlDto> nextPageUrls) {
    if (nextPageUrls.isEmpty()) {
      return;
    }

    final ConcurrentLinkedQueue<KikaCrawlerUrlDto> nextPageLinks = new ConcurrentLinkedQueue<>(nextPageUrls);
    final AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> subPageCrawler =
        createNewOwnInstance(nextPageLinks, pageNumber + 1);
    subPageCrawler.fork();
    final Set<KikaCrawlerUrlDto> join = subPageCrawler.join();
    taskResults.addAll(join);
  }

  private void parseFilmUrls(final Document aDocument, FilmType filmType) {
    Elements urlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW_WITH_BOXBROADCAST);
    if (urlElements.isEmpty()) {
      urlElements = aDocument.select(SELECTOR_TOPIC_OVERVIEW_NO_BOXBROADCAST);
    }
    for (final Element urlElement : urlElements) {
      final String url = urlElement.attr(Consts.ATTRIBUTE_HREF);
      final Element iconElement = urlElement.parent().select(SELECTOR_TYPE_ICON).first();
      if (iconElement != null && iconElement.text().equals(ENTRY_ICON_FILM)) {
        taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), filmType));
      }
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aElementsToProcess) {
    return createNewOwnInstance(aElementsToProcess, 1);
  }

  private AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aElementsToProcess, final int aPageNumber) {
    return new KikaTopicOverviewPageTask(
        crawler, aElementsToProcess, baseUrl, getJsoupConnection(), aPageNumber);
  }

  private List<KikaCrawlerUrlDto> parseNextPageUrls(final Document aDocument, FilmType filmType) {
    final List<KikaCrawlerUrlDto> nextPages = new ArrayList<>();

    final Elements subPageElements = aDocument.select(SELECTOR_SUBPAGES);
    for (final Element subPageElement : subPageElements) {
      final String url = subPageElement.attr(Consts.ATTRIBUTE_HREF);
      nextPages.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), filmType));
    }

    return nextPages;
  }
}
