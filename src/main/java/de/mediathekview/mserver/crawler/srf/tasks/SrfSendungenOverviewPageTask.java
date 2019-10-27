package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.crawler.srf.SrfShowOverviewUrlBuilder;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungenOverviewJsonDeserializer;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class SrfSendungenOverviewPageTask
    implements Callable<ConcurrentLinkedQueue<CrawlerUrlDTO>> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungenOverviewPageTask.class);
  private static final String JSON_SELECTOR = "div.showsAtoZContent";
  private static final String ATTRIBUTE_DATA = "data-alphabetical-sections";

  private final SrfShowOverviewUrlBuilder urlBuilder = new SrfShowOverviewUrlBuilder();
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public SrfSendungenOverviewPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  JsoupConnection jsoupConnection = new JsoupConnection();

  @Override
  public ConcurrentLinkedQueue<CrawlerUrlDTO> call() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> results = new ConcurrentLinkedQueue<>();

    try {
      final Document document =
          jsoupConnection.getDocumentTimeoutAfter(SrfConstants.OVERVIEW_PAGE_URL,
              (int) TimeUnit.SECONDS.toMillis(
                  crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
      final Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(Set.class, new SrfSendungenOverviewJsonDeserializer(urlBuilder))
              .create();

      document
          .select(JSON_SELECTOR)
          .forEach(
              dataElement -> {
                if (dataElement.hasAttr(ATTRIBUTE_DATA)) {
                  final String jsonData = dataElement.attr(ATTRIBUTE_DATA);

                  results.addAll(gson.fromJson(jsonData, Set.class));
                  results.addAll(addSpecialShows());

                } else {
                  LOG.error(
                      "element '"
                          + JSON_SELECTOR
                          + "' does not have attribute '"
                          + ATTRIBUTE_DATA
                          + "'.");
                }
              });
    } catch (final IOException ioException) {
      LOG.fatal("Error parsing SRF overview page.", ioException);
    }

    return results;
  }

  private Set<CrawlerUrlDTO> addSpecialShows() {
    final Set<CrawlerUrlDTO> shows = new HashSet<>();
    shows.add(new CrawlerUrlDTO(urlBuilder.buildUrl(SrfConstants.ID_SHOW_SPORT_CLIP)));

    return shows;
  }
}
