package mServer.crawler.sender.tagesschau.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.tagesschau.json.TagesschauVideoDeserializer;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class TagesschauVideoTask extends AbstractDocumentTask<DatenFilm, CrawlerUrlDTO> {
  private static final String DESCRIPTOR_MEDIA_PLAYER = "div[data-v-type=MediaPlayer]";
  private static final Logger LOG = LogManager.getLogger(TagesschauVideoTask.class);

  private static final Type FILM_TYPE_TOKEN = new TypeToken<List<DatenFilm>>() {}.getType();

  public TagesschauVideoTask(MediathekReader crawler, ConcurrentLinkedQueue<CrawlerUrlDTO> queue) {
    super(crawler, queue);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    if (Config.getStop()) {
      return;
    }

    final Gson gson =
            new GsonBuilder()
                    .registerTypeAdapter(FILM_TYPE_TOKEN, new TagesschauVideoDeserializer())
                    .create();

    aDocument
        .select(DESCRIPTOR_MEDIA_PLAYER)
        .forEach(
            element -> {
              try {
                String json = element.attr("data-v");
                final List<DatenFilm> films = gson.fromJson(json, FILM_TYPE_TOKEN);
                taskResults.addAll(films);
              } catch (Exception e) {
                LOG.error(e);
                Log.errorLog(346234838, e, aUrlDTO.getUrl());
              }
            });
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauVideoTask(crawler, aElementsToProcess);
  }
}
