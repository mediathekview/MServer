package de.mediathekview.mserver.crawler.kika.tasks;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;

public class KikaPagedOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = -6541384259764770529L;
  private static final String SENDUNG_URL_SELECTOR = ".teaser .linkAll";
  private static final String SUBPAGE_URL_SELECTOR = ".bundleNaviItem a";
  private boolean incrementMaxCount;

  public KikaPagedOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    incrementMaxCount = false;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> gatherSubpageUrls(final Document aDocument) {
    final Set<CrawlerUrlDTO> subpageUrls = new HashSet<>();
    for (final Element filmUrlElement : aDocument.select(SUBPAGE_URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        subpageUrls.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)));
      }
    }
    return new ConcurrentLinkedQueue<>(subpageUrls);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaPagedOverviewPageTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Optional<AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO>> subpageTask;
    if (aUrlDTO.getUrl().equals(KikaCrawler.SENDUNGEN_OVERVIEW_PAGE_URL)) {
      subpageTask = Optional.of(createNewOwnInstance(gatherSubpageUrls(aDocument)));
      subpageTask.get().fork();
    } else {
      subpageTask = Optional.empty();
    }

    for (final Element filmUrlElement : aDocument.select(SENDUNG_URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)));
        if (incrementMaxCount) {
          crawler.incrementAndGetMaxCount();
          crawler.updateProgress();
        }
      }
    }

    if (subpageTask.isPresent()) {
      taskResults.addAll(subpageTask.get().join());
    }
  }

  protected void setIncrementMaxCount(final boolean aIncrementMaxCount) {
    incrementMaxCount = aIncrementMaxCount;
  }

}
