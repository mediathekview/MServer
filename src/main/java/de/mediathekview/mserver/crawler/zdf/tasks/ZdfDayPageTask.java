package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDto;

import jakarta.annotation.Nullable;
import jakarta.ws.rs.client.WebTarget;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ZdfDayPageTask extends ZdfTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private final String apiUrlBase;

  public ZdfDayPageTask(
      final AbstractCrawler crawler,
      final String apiUrlBase,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      @Nullable final String authKey) {
    super(crawler, urlToCrawlDTOs, authKey);
    this.apiUrlBase = apiUrlBase;
    registerJsonDeserializer(ZdfDayPageDto.class, new ZdfDayPageDeserializer(this.apiUrlBase));
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
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfDayPageTask(crawler, apiUrlBase, aElementsToProcess, getAuthKey().orElse(null));
  }

  private void processNextPage(final ZdfDayPageDto entries) {
    if (entries.getNextPageUrl().isPresent() && !entries.getEntries().isEmpty()) {
      final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
      urls.add(new CrawlerUrlDTO(entries.getNextPageUrl().get()));
      taskResults.addAll(createNewOwnInstance(urls).invoke());
    }
  }
}
