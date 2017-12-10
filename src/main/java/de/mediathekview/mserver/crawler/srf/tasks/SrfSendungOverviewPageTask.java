package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrfSendungOverviewPageTask extends AbstractRestTask<SrfSendungOverviewDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungOverviewPageTask.class);
  
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
    
    if (response.getStatus() == 200) {

      final String jsonOutput = response.readEntity(String.class);

      try {
        taskResults.add(gson.fromJson(jsonOutput, SrfSendungOverviewDTO.class));
      } catch (JsonSyntaxException e) {
        LOG.error("Error reading url " + aTarget.getUri().toString(), e);
      }
      
      // TODO next page...
    } else {
      LOG.error("Error reading url " + aTarget.getUri().toString() + ": " + response.getStatus());
    }
  }

  @Override
  protected AbstractUrlTask createNewOwnInstance(ConcurrentLinkedQueue aURLsToCrawl) {
    return new SrfSendungOverviewPageTask(crawler, aURLsToCrawl);
  }
}