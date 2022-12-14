package de.mediathekview.mserver.crawler.dw.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.dw.DWTaskBase;
import de.mediathekview.mserver.crawler.dw.parser.DWSendungOverviewDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DWOverviewTask extends DWTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PagedElementListDTO<CrawlerUrlDTO>>>() {}.getType();
  private final int subpage;

  public DWOverviewTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final int subpage)
  {
	  super(crawler, urlToCrawlDTOs, null);
	  this.subpage = subpage;
	  registerJsonDeserializer(
	    OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new DWSendungOverviewDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PagedElementListDTO<CrawlerUrlDTO>> overviewDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
    if (overviewDtoOptional.isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final PagedElementListDTO<CrawlerUrlDTO> overviewDto = overviewDtoOptional.get();
    addResults(overviewDto.getElements());

    final Optional<String> optionalNextPage = overviewDto.getNextPage();
    if (optionalNextPage.isPresent() && subpage < config.getMaximumSubpages()) {
      final String nextPage = optionalNextPage.get();
      if (!aDTO.getUrl().endsWith(nextPage)) {
    	final Queue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    	queue.add(new CrawlerUrlDTO(nextPage));
        taskResults.addAll(createNewOwnInstance(queue).invoke());
      }
    }
  }

  private void addResults(final Collection<CrawlerUrlDTO> aUrls) {
    for (final CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(url.getUrl()));
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new DWOverviewTask(crawler, aElementsToProcess, subpage + 1);
  }

}
