package de.mediathekview.mserver.crawler.ndr.tasks;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.ndr.parser.NdrFilmDeserializer;

public class NdrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {
  private static final String SRC_ARGUMENT = "src";
  private static final String TIME_PATTERN = "dd.MM.yyyy hh:mm Uhr";
  private static final String IFRAME_SELECTOR = ".stagePlayer iframe";
  private static final String TIME_SELECTOR = ".textinfo .subline span:eq(2)";
  private static final String THEMA_SELECTOR = ".textinfo .subline span:eq(1)";
  private static final String TITLE_SELECTOR = ".textinfo h1";
  private static final long serialVersionUID = 1614807484305273437L;
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final Type OPTIONAL_FILM_TYPE_TOKEN = new TypeToken<Optional<Film>>() {}.getType();
  private final Client client;

  public NdrSendungsfolgedetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  private String extractAdditionalVideoInfoUrl(final Element aIframeElement) {
    // From
    // http://www.ndr.de/fernsehen/sendungen/sportclub/schwenker172-ardplayer_image-58390aa6-8e8a-458b-b3a7-d7b23e91e186_theme-ndrde.html
    // To
    // http://www.ndr.de/fernsehen/sendungen/sportclub/schwenker172-ardjson_image-58390aa6-8e8a-458b-b3a7-d7b23e91e186.json
    final String playerUrl = aIframeElement.absUrl(SRC_ARGUMENT);
    return playerUrl.replaceAll("ardplayer", "ardjson").replaceAll("_theme-ndrde.html", ".json");
  }

  private LocalDateTime parseTime(final String aText) {
    // Parse dates like: 12.11.2017 23:15 Uhr
    return LocalDateTime.parse(aText, DateTimeFormatter.ofPattern(TIME_PATTERN));
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new NdrSendungsfolgedetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {

    final Elements titleElement = aDocument.select(TITLE_SELECTOR);
    final String titel = titleElement.first().text();
    final Elements themaElement = aDocument.select(THEMA_SELECTOR);
    final String thema = themaElement.first().text();
    final Elements timeElement = aDocument.select(TIME_SELECTOR);
    final LocalDateTime time = parseTime(timeElement.text());
    final String additionalVideoInfoUrl =
        extractAdditionalVideoInfoUrl(aDocument.select(IFRAME_SELECTOR).first());

    final WebTarget target = client.target(additionalVideoInfoUrl);
    final Response response = target.request().header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
    final String jsonOutput = response.readEntity(String.class);

    final Gson gson = new GsonBuilder().registerTypeAdapter(OPTIONAL_FILM_TYPE_TOKEN,
        new NdrFilmDeserializer(crawler, aUrlDTO.getUrl(), titel, thema, time)).create();

    final Optional<Film> newFilm = gson.fromJson(jsonOutput, OPTIONAL_FILM_TYPE_TOKEN);
    if (newFilm.isPresent()) {
      final Elements descriptionElement = aDocument.select(".textinfo p");
      if (!descriptionElement.isEmpty()) {
        newFilm.get().setBeschreibung(descriptionElement.first().text());
      }
      taskResults.add(newFilm.get());
      crawler.incrementAndGetActualCount();
    } else {
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
    }
    crawler.updateProgress();
  }

}
