package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDto;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ZdfDayPageTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final String apiUrlBase;

  public ZdfDayPageTask(
      final AbstractCrawler aCrawler,
      final String aApiUrlBase,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    apiUrlBase = aApiUrlBase;
    registerJsonDeserializer(ZdfDayPageDto.class, new ZdfDayPageDeserializer(apiUrlBase));
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDto, final WebTarget aTarget) {
    final ZdfDayPageDto dto = deserialize(aTarget, ZdfDayPageDto.class);
    if (dto != null) {
      taskResults.addAll(dto.getEntries());
      processNextPage(dto);
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageTask(crawler, apiUrlBase, aElementsToProcess, authKey);
  }

  private void processNextPage(final ZdfDayPageDto entries) {
    if (entries.getNextPageUrl().isPresent() && !entries.getEntries().isEmpty()) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
      urls.add(new CrawlerUrlDTO(entries.getNextPageUrl().get()));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }
}
