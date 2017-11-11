package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewDTO;

public abstract class AbstractFunkOverviewTask extends
    AbstractFunkRestTask<FunkOverviewDTO<FunkSendungDTO>, FunkOverviewDTO<FunkSendungDTO>, CrawlerUrlDTO> {
  private static final String PARAMETER_PAGE = "page";
  private static final long serialVersionUID = -3525701804445428824L;

  public AbstractFunkOverviewTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }


  @Override
  protected Type getType() {
    return new TypeToken<FunkOverviewDTO<FunkSendungDTO>>() {}.getType();
  }



  @Override
  protected void postProcessing(final FunkOverviewDTO<FunkSendungDTO> aResponseObj,
      final CrawlerUrlDTO aUrlDTO) {
    taskResults.add(aResponseObj);

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
