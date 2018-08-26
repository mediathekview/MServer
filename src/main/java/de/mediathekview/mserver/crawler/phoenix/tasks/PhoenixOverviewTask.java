package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.common.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.SendungOverviewDto;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixSendungOverviewDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class PhoenixOverviewTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN = new TypeToken<Optional<SendungOverviewDto>>() {
  }.getType();


  private final String baseUrl;
  private final int subpage;

  public PhoenixOverviewTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, Optional<String> aAuthKey, String aBaseUrl) {
    this(aCrawler, aUrlToCrawlDtos, aAuthKey, aBaseUrl, 0);
  }

  private PhoenixOverviewTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, Optional<String> aAuthKey, String aBaseUrl, int aSubpage) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);

    baseUrl = aBaseUrl;
    subpage = aSubpage;

    registerJsonDeserializer(OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new PhoenixSendungOverviewDeserializer());
  }


  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Optional<SendungOverviewDto> overviewDtoOptional = deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
    if (!overviewDtoOptional.isPresent()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    SendungOverviewDto overviewDto = overviewDtoOptional.get();
    addResults(overviewDto.getUrls());

    if (overviewDto.getNextPageId().isPresent() && subpage < this.config.getMaximumSubpages()) {
      taskResults.addAll(createNewOwnInstance(baseUrl + overviewDto.getNextPageId().get()).invoke());
    }
  }

  private void addResults(Collection<CrawlerUrlDTO> aUrls) {
    for (CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(baseUrl + url.getUrl()));
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixOverviewTask(crawler, aElementsToProcess, authKey, baseUrl, subpage + 1);
  }

  private AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(final String aUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(aUrl));
    return createNewOwnInstance(queue);
  }
}
