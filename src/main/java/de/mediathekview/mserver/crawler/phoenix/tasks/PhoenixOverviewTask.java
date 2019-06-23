package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixSendungOverviewDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;

import javax.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhoenixOverviewTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN =
          new TypeToken<Optional<PagedElementListDTO<CrawlerUrlDTO>>>() {
          }.getType();

  private final String baseUrl;
  private final int subpage;

  public PhoenixOverviewTask(
          final AbstractCrawler aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final Optional<String> aAuthKey,
          final String aBaseUrl) {
    this(aCrawler, aUrlToCrawlDtos, aAuthKey, aBaseUrl, 0);
  }

  private PhoenixOverviewTask(
          final AbstractCrawler aCrawler,
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
          final Optional<String> aAuthKey,
          final String aBaseUrl,
          final int aSubpage) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);

    baseUrl = aBaseUrl;
    subpage = aSubpage;

    registerJsonDeserializer(
            OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new PhoenixSendungOverviewDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PagedElementListDTO<CrawlerUrlDTO>> overviewDtoOptional =
            deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
    if (!overviewDtoOptional.isPresent()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final PagedElementListDTO<CrawlerUrlDTO> overviewDto = overviewDtoOptional.get();
    addResults(overviewDto.getElements());

    if (overviewDto.getNextPage().isPresent() && subpage < config.getMaximumSubpages()
            // Workaround to fix paging problem in Phönix-API
            && !aDTO.getUrl().endsWith(overviewDto.getNextPage().get())) {
      taskResults.addAll(createNewOwnInstance(baseUrl + overviewDto.getNextPage().get()).invoke());
    }
  }

  private void addResults(final Collection<CrawlerUrlDTO> aUrls) {
    for (final CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(baseUrl + url.getUrl()));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixOverviewTask(crawler, aElementsToProcess, authKey, baseUrl, subpage + 1);
  }

  private AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          final String aUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(aUrl));
    return createNewOwnInstance(queue);
  }
}
