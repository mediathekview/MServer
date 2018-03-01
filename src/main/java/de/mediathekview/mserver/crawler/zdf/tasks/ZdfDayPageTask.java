package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDeserializer;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDayPageDto;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class ZdfDayPageTask extends AbstractRestTask<ZdfEntryDto, CrawlerUrlDTO> {

  private final Gson gson;

  public ZdfDayPageTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    gson = new GsonBuilder()
        .registerTypeAdapter(ZdfDayPageDto.class, new ZdfDayPageDeserializer())
        .create();
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {

    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }

    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      final ZdfDayPageDto entries = gson.fromJson(jsonOutput, ZdfDayPageDto.class);
      taskResults.addAll(entries.getEntries());

      processNextPage(entries);
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
