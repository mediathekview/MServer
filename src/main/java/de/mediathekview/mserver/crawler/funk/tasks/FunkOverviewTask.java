package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewUrlDTODeserializer;

public class FunkOverviewTask extends AbstractFunkRestTask<FunkOverviewDTO> {
  private static final String PARAMETER_PAGE = "page";
  private static final long serialVersionUID = -3525701804445428824L;

  public FunkOverviewTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final boolean aIncrementMaxCount,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aIncrementMaxCount, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<FunkOverviewDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new FunkOverviewTask(crawler, aURLsToCrawl, incrementMaxCount, authKey);
  }

  @Override
  protected Object getParser() {
    return new FunkOverviewUrlDTODeserializer();
  }

  @Override
  protected Type getType() {
    return FunkOverviewDTO.class;
  }

  @Override
  protected void postProcessing(final FunkOverviewDTO aResponseObj, final CrawlerUrlDTO aUrlDTO) {
    final Optional<Integer> nextPageId = aResponseObj.getNextPageId();
    if (nextPageId.isPresent() && nextPageId.get() <= config.getMaximumSubpages()) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = new ConcurrentLinkedQueue<>();
      final String newUrl = UrlUtils.changeOrAddParameter(aUrlDTO.getUrl(), PARAMETER_PAGE,
          nextPageId.get().toString());
      subpages.add(new CrawlerUrlDTO(newUrl));
      taskResults.addAll(createNewOwnInstance(subpages).invoke());
    }
  }

}
