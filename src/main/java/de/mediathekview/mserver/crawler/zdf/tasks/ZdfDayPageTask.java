package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDto;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;

public class ZdfDayPageTask extends ZdfTaskBase<ZdfEntryDto, CrawlerUrlDTO> {

  public ZdfDayPageTask(AbstractCrawler aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos, Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    registerJsonDeserializer(ZdfDayPageDto.class, new ZdfDayPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDto, WebTarget aTarget) {

    ZdfDayPageDto dto = deserialize(aTarget, ZdfDayPageDto.class);
    if (dto != null) {
      taskResults.addAll(dto.getEntries());
      processNextPage(dto);
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<ZdfEntryDto, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageTask(crawler, aElementsToProcess, authKey);
  }

  private void processNextPage(final ZdfDayPageDto entries) {
    if (entries.getNextPageUrl().isPresent()) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
      urls.add(new CrawlerUrlDTO(entries.getNextPageUrl().get()));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }
}
