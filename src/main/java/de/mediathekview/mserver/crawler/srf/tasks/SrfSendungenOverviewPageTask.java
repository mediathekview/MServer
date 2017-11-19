package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungenOverviewJsonDeserializer;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SrfSendungenOverviewPageTask implements Callable<Set<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewPageTask.class);
  public static final String OVERVIEW_PAGE_URL = "https://www.srf.ch/play/v2/tv/shows";
  private static final String JSON_SELECTOR = "div.showsAtoZContent";
  private static final String ATTRIBUTE_DATA = "data-alphabetical-sections";
  
  @Override
  public Set<CrawlerUrlDTO> call() throws Exception {
    final Set<CrawlerUrlDTO> results = new HashSet<>();
    
    try {
      final Document document = Jsoup.connect(OVERVIEW_PAGE_URL).get();
      
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
