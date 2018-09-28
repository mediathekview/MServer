package mServer.crawler.sender.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.orf.CrawlerUrlDTO;
import mServer.crawler.sender.orf.tasks.AbstractRecursivConverterTask;
import mServer.crawler.sender.phoenix.SendungOverviewDto;
import mServer.crawler.sender.phoenix.parser.PhoenixSendungOverviewDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhoenixOverviewTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(PhoenixOverviewTask.class);

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN = new TypeToken<Optional<SendungOverviewDto>>() {
  }.getType();

  private final String baseUrl;
  private final int subpage;

  public PhoenixOverviewTask(MediathekReader aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, Optional<String> aAuthKey, String aBaseUrl) {
    this(aCrawler, aUrlToCrawlDtos, aAuthKey, aBaseUrl, 0);
  }

  public PhoenixOverviewTask(MediathekReader aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, Optional<String> aAuthKey, String aBaseUrl, int aSubpage) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);

    baseUrl = aBaseUrl;
    subpage = aSubpage;

    registerJsonDeserializer(OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new PhoenixSendungOverviewDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    try {
      Optional<SendungOverviewDto> overviewDtoOptional = deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
      if (!overviewDtoOptional.isPresent()) {
        LOG.fatal("PhoenixOverviewTask: error processing url " + aDTO.getUrl());
        return;
      }

      SendungOverviewDto overviewDto = overviewDtoOptional.get();
      addResults(overviewDto.getUrls());

      if (overviewDto.getNextPageId().isPresent()) {
        taskResults.addAll(createNewOwnInstance(baseUrl + overviewDto.getNextPageId().get()).invoke());
      }
    } catch (Exception e) {
      LOG.fatal(e);
    }
  }

  private void addResults(Collection<CrawlerUrlDTO> aUrls) {
    for (CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(baseUrl + url.getUrl()));
    }
  }

  @Override
  protected AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixOverviewTask(crawler, aElementsToProcess, authKey, baseUrl, subpage + 1);
  }

  private AbstractRecursivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(final String aUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(aUrl));
    return createNewOwnInstance(queue);
  }
}
