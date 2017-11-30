package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungOverviewDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungOverviewJsonDeserializer;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

public class SrfSendungOverviewPageTask extends AbstractRestTask<SrfSendungOverviewDTO, CrawlerUrlDTO> {

  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  
  public SrfSendungOverviewPageTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    final Gson gson = new GsonBuilder().registerTypeAdapter(SrfSendungOverviewDTO.class, new SrfSendungOverviewJsonDeserializer()).create();
    Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    final String jsonOutput = response.readEntity(String.class);
    taskResults.add(gson.fromJson(jsonOutput, SrfSendungOverviewDTO.class));
    
    // TODO next page...
  }

  @Override
  protected AbstractUrlTask createNewOwnInstance(ConcurrentLinkedQueue aURLsToCrawl) {
    return new SrfSendungOverviewPageTask(crawler, aURLsToCrawl);
  }
}