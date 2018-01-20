package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungenOverviewJsonDeserializer;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SrfSendungenOverviewPageTask implements Callable<ConcurrentLinkedQueue<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewPageTask.class);
  private static final String JSON_SELECTOR = "div.showsAtoZContent";
  private static final String ATTRIBUTE_DATA = "data-alphabetical-sections";
  
  @Override
  public ConcurrentLinkedQueue<CrawlerUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> results = new ConcurrentLinkedQueue<>();
    
    try {
      final Document document = Jsoup.connect(SrfConstants.OVERVIEW_PAGE_URL).get();
      
      Gson gson = new GsonBuilder()
        .registerTypeAdapter(Set.class, new SrfSendungenOverviewJsonDeserializer())
        .create();
      
      document.select(JSON_SELECTOR).forEach((dataElement) -> {
        if (dataElement.hasAttr(ATTRIBUTE_DATA)) {
          String jsonData = dataElement.attr(ATTRIBUTE_DATA);
          
          results.addAll(gson.fromJson(jsonData, Set.class));
          
        } else {
          LOG.error("element '" + JSON_SELECTOR + "' does not have attribute '" + ATTRIBUTE_DATA + "'.");
        }
      });
    } catch (final IOException ioException) {
      LOG.fatal(
          "Error parsing SRF overview page.",
          ioException);
    }

    return results;
  }
  
}
