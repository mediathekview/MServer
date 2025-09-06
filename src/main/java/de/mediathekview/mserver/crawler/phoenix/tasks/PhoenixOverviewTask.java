package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixSendungOverviewDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PhoenixOverviewTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PagedElementListDTO<CrawlerUrlDTO>>>() {}.getType();

  private final String baseUrl;
  private final int subpage;

  public PhoenixOverviewTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      @Nullable final String authKey,
      final String baseUrl) {
    this(crawler, urlToCrawlDTOs, authKey, baseUrl, 0);
  }

  private PhoenixOverviewTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      @Nullable final String authKey,
      final String baseUrl,
      final int aSubpage) {
    super(crawler, urlToCrawlDTOs, authKey);

    this.baseUrl = baseUrl;
    subpage = aSubpage;

    registerJsonDeserializer(
        OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new PhoenixSendungOverviewDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PagedElementListDTO<CrawlerUrlDTO>> overviewDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
    if (overviewDtoOptional.isEmpty()) {
      return;
    }

    final PagedElementListDTO<CrawlerUrlDTO> overviewDto = overviewDtoOptional.get();
    addResults(overviewDto.getElements());

    final Optional<String> optionalNextPage = overviewDto.getNextPage();
    if (optionalNextPage.isPresent() && subpage < config.getMaximumSubpages()) {
      final String nextPage = optionalNextPage.get();
      if (!aDTO.getUrl().endsWith(nextPage)) {
        taskResults.addAll(createNewOwnInstance(baseUrl + nextPage).invoke());
      }
    }
  }

  private void addResults(final Collection<CrawlerUrlDTO> aUrls) {
    for (final CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(baseUrl + url.getUrl()));
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixOverviewTask(
        crawler, aElementsToProcess, getAuthKey().orElse(null), baseUrl, subpage + 1);
  }

  private AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final String aUrl) {
    final Queue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(aUrl));
    return createNewOwnInstance(queue);
  }
}
