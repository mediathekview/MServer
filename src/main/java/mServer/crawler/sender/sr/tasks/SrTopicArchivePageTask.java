package mServer.crawler.sender.sr.tasks;

import mServer.crawler.sender.sr.SrConstants;
import mServer.crawler.sender.sr.SrTopicUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.AbstractDocumentTask;

public class SrTopicArchivePageTask extends AbstractDocumentTask<SrTopicUrlDTO, SrTopicUrlDTO> {

  private static final String ATTRIBUTE_HREF = "href";

  private static final String NEXT_PAGE_SELECTOR = "div.pagination__item > a[title*=weiter]";
  private static final String SHOW_LINK_SELECTOR = "h3.teaser__text__header a";
  private final int pageNumber;
  private final int maxNumberSubpages;

  public SrTopicArchivePageTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<SrTopicUrlDTO> aUrlToCrawlDTOs) {
    this(aCrawler, aUrlToCrawlDTOs, 1);
  }

  public SrTopicArchivePageTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<SrTopicUrlDTO> aUrlToCrawlDTOs,
          final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDTOs);
    pageNumber = aPageNumber;

    if (CrawlerTool.loadLongMax()) {
      maxNumberSubpages = 10;
    } else {
      maxNumberSubpages = 3;
    }
  }

  @Override
  protected void processDocument(final SrTopicUrlDTO aUrlDTO, final Document aDocument) {
    parsePage(aUrlDTO.getTheme(), aDocument);

    final Optional<String> nextPageUrl = getNextPage(aDocument);
    if (nextPageUrl.isPresent() && pageNumber < maxNumberSubpages) {
      processNextPage(aUrlDTO.getTheme(), nextPageUrl.get());
    }
  }

  @Override
  protected AbstractUrlTask<SrTopicUrlDTO, SrTopicUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<SrTopicUrlDTO> aURLsToCrawl) {
    return new SrTopicArchivePageTask(crawler, aURLsToCrawl);
  }

  private AbstractUrlTask<SrTopicUrlDTO, SrTopicUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<SrTopicUrlDTO> aURLsToCrawl, final int aPageNumber) {
    return new SrTopicArchivePageTask(crawler, aURLsToCrawl, aPageNumber);
  }

  private void parsePage(final String aTheme, final Document aDocument) {
    final Elements links = aDocument.select(SHOW_LINK_SELECTOR);
    links.forEach(
            element -> {
              final String url = element.attr(ATTRIBUTE_HREF);
              taskResults.add(new SrTopicUrlDTO(aTheme, SrConstants.URL_BASE + url));
            });
  }

  private Optional<String> getNextPage(final Document aDocument) {
    final Elements links = aDocument.select(NEXT_PAGE_SELECTOR);
    if (links.size() == 1) {
      return Optional.of(SrConstants.URL_BASE + links.attr(ATTRIBUTE_HREF));
    }

    return Optional.empty();
  }

  private void processNextPage(final String aTheme, final String aNextPageId) {
    final ConcurrentLinkedQueue<SrTopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new SrTopicUrlDTO(aTheme, aNextPageId));
    final Set<SrTopicUrlDTO> x = createNewOwnInstance(urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(x);
  }
}
