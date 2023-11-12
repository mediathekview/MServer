package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SrTopicArchivePageTask
    extends AbstractDocumentTask<SrTopicUrlDTO, SrTopicUrlDTO> {

  private static final String NEXT_PAGE_SELECTOR = "div.pagination__item > a[title*=weiter]";
  private static final String SHOW_SELECTOR = "h3.teaser__text__header";
  private static final String TYPE_SELECTOR = "span.teaser__text__header__element--subhead";

  private final int pageNumber;

  public SrTopicArchivePageTask(
      final AbstractCrawler aCrawler,
      final Queue<SrTopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    pageNumber = 1;
  }

  public SrTopicArchivePageTask(
      final AbstractCrawler aCrawler,
      final Queue<SrTopicUrlDTO> aUrlToCrawlDTOs,
      final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDTOs);
    pageNumber = aPageNumber;
  }

  @Override
  protected void processDocument(final SrTopicUrlDTO aUrlDTO, final Document aDocument) {
    parsePage(aUrlDTO.getTheme(), aDocument);

    final Optional<String> nextPageUrl = getNextPage(aDocument);
    if (nextPageUrl.isPresent() && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
      processNextPage(aUrlDTO.getTheme(), nextPageUrl.get());
    }
  }

  @Override
  protected AbstractUrlTask<SrTopicUrlDTO, SrTopicUrlDTO> createNewOwnInstance(
      final Queue<SrTopicUrlDTO> aURLsToCrawl) {
    return new SrTopicArchivePageTask(crawler, aURLsToCrawl);
  }

  private AbstractUrlTask<SrTopicUrlDTO, SrTopicUrlDTO> createNewOwnInstance(
      final Queue<SrTopicUrlDTO> aURLsToCrawl, final int aPageNumber) {
    return new SrTopicArchivePageTask(crawler, aURLsToCrawl, aPageNumber);
  }

  private void parsePage(final String aTheme, final Document aDocument) {
    final Elements shows = aDocument.select(SHOW_SELECTOR);
    shows.forEach(
        element -> {
          // ignore audio files
          if (!isAudioShow(element)) {
            final Elements urlElements = element.getElementsByTag("a");
            if (!urlElements.isEmpty()) {
              final String url = urlElements.first().attr(HtmlConsts.ATTRIBUTE_HREF);
              taskResults.add(new SrTopicUrlDTO(aTheme, SrConstants.URL_BASE + url));
            }
          }
        });
  }

  private boolean isAudioShow(final Element show) {
    final Elements selected = show.select(TYPE_SELECTOR);
    return !selected.isEmpty() && selected.first().text().contains("Audio");
  }

  private Optional<String> getNextPage(final Document aDocument) {
    final Elements links = aDocument.select(NEXT_PAGE_SELECTOR);
    if (links.size() == 1) {
      return Optional.of(SrConstants.URL_BASE + links.attr(HtmlConsts.ATTRIBUTE_HREF));
    }

    return Optional.empty();
  }

  private void processNextPage(final String aTheme, final String aNextPageId) {
    final Queue<SrTopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new SrTopicUrlDTO(aTheme, aNextPageId));
    final Set<SrTopicUrlDTO> x = createNewOwnInstance(urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(x);
  }
}
