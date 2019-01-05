package mServer.crawler.sender.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mServer.crawler.sender.srf.SrfConstants;
import mServer.crawler.sender.srf.SrfShowOverviewUrlBuilder;
import mServer.crawler.sender.srf.parser.SrfSendungenOverviewJsonDeserializer;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SrfSendungenOverviewPageTask implements Callable<ConcurrentLinkedQueue<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewPageTask.class);
  private static final String JSON_SELECTOR = "div.showsAtoZContent";
  private static final String ATTRIBUTE_DATA = "data-alphabetical-sections";

  private final SrfShowOverviewUrlBuilder urlBuilder;

  public SrfSendungenOverviewPageTask() {

    this.urlBuilder = new SrfShowOverviewUrlBuilder(CrawlerTool.loadLongMax() ? 100 : 30);
  }

  @Override
  public ConcurrentLinkedQueue<CrawlerUrlDTO> call() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> results = new ConcurrentLinkedQueue<>();

    try {
      final Document document = Jsoup.connect(SrfConstants.OVERVIEW_PAGE_URL).get();

      Gson gson = new GsonBuilder()
              .registerTypeAdapter(Set.class, new SrfSendungenOverviewJsonDeserializer(urlBuilder))
              .create();

      document.select(JSON_SELECTOR).forEach(dataElement -> {
        if (dataElement.hasAttr(ATTRIBUTE_DATA)) {
          String jsonData = dataElement.attr(ATTRIBUTE_DATA);

          results.addAll(gson.fromJson(jsonData, Set.class));
          results.addAll(addSpecialShows());

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

  private Set<CrawlerUrlDTO> addSpecialShows() {
    Set<CrawlerUrlDTO> shows = new HashSet<>();
    shows.add(new CrawlerUrlDTO(urlBuilder.buildUrl(SrfConstants.ID_SHOW_SPORT_CLIP)));

    return shows;
  }

}
