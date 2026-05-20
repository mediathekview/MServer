package de.mediathekview.mserver.crawler.tagesschau.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.json.TagesschauVideoDeserializer;
import de.mediathekview.mserver.daten.Film;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class TagesschauVideoTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {
  private static final String DESCRIPTOR_MEDIA_PLAYER = "div[data-v-type=MediaPlayer]";
  private static final Logger LOG = LogManager.getLogger(TagesschauVideoTask.class);

  private static final Type FILM_TYPE_TOKEN = new TypeToken<List<Film>>() {}.getType();

  public TagesschauVideoTask(AbstractCrawler crawler, Queue<CrawlerUrlDTO> queue) {
    super(crawler, queue);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(FILM_TYPE_TOKEN, new TagesschauVideoDeserializer(crawler))
                    .create();

    aDocument
        .select(DESCRIPTOR_MEDIA_PLAYER)
        .forEach(
            element -> {
              try {
                String json = element.attr("data-v");
                final List<Film> films = gson.fromJson(json, FILM_TYPE_TOKEN);
                taskResults.addAll(films);
                crawler.incrementAndGetActualCount();
              } catch (Exception e) {
                LOG.error(e);
                crawler.incrementAndGetErrorCount();
              }
            });
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauVideoTask(crawler, aElementsToProcess);
  }
}
