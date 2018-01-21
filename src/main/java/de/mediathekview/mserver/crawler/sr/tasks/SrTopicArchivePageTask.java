package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class SrTopicArchivePageTask extends AbstractDocumentTask<SrTopicUrlDTO, SrTopicUrlDTO> {

  private static final String NEXT_PAGE_SELECTOR = "div.pagination__item > a[title*=weiter]";
  private static final String SHOW_LINK_SELECTOR = "h3.teaser__text__header a";
  
  public SrTopicArchivePageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<SrTopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }
  
  @Override
  protected void processDocument(SrTopicUrlDTO aUrlDTO, Document aDocument) {
    parsePage(aUrlDTO.getTheme(), aDocument);    

    Optional<String> nextPageUrl = getNextPage(aDocument);
    if (nextPageUrl.isPresent()) {
      processNextPage(aUrlDTO.getTheme(), nextPageUrl.get());
    }
  }

  @Override
  protected AbstractUrlTask<SrTopicUrlDTO, SrTopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<SrTopicUrlDTO> aURLsToCrawl) {
    return new SrTopicArchivePageTask(crawler, aURLsToCrawl);
  }  
  
  private void parsePage(String aTheme, Document aDocument) {
    Elements links = aDocument.select(SHOW_LINK_SELECTOR);
    links.forEach(element -> {
      String url = element.attr(Consts.ATTRIBUTE_HREF);
      this.taskResults.add(new SrTopicUrlDTO(aTheme, SrConstants.URL_BASE + url));
    });    
  }
  
  private Optional<String> getNextPage(Document aDocument) {
    Elements links = aDocument.select(NEXT_PAGE_SELECTOR);
    if (links.size() == 1) {
      return Optional.of(SrConstants.URL_BASE + links.attr(Consts.ATTRIBUTE_HREF));
    }
    
    return Optional.empty();
  }
  
  private void processNextPage(String aTheme, String aNextPageId) {
    final ConcurrentLinkedQueue<SrTopicUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new SrTopicUrlDTO(aTheme, aNextPageId));
    Set<SrTopicUrlDTO> x = createNewOwnInstance(urlDtos).invoke();
    taskResults.addAll(x);
  }  
}
