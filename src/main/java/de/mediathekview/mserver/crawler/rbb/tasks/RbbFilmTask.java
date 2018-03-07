package de.mediathekview.mserver.crawler.rbb.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDTO;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.parser.RbbFilmDetailDeserializer;
import de.mediathekview.mserver.crawler.rbb.parser.RbbFilmInfoDto;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class RbbFilmTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(RbbFilmTask.class);

  private final RbbFilmDetailDeserializer filmDetailDeserializer;
  private final Gson gson;

  public RbbFilmTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    filmDetailDeserializer = new RbbFilmDetailDeserializer();
    gson = new GsonBuilder()
        .registerTypeAdapter(ArdVideoInfoDTO.class, new ArdVideoInfoJsonDeserializer(crawler))
        .create();
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDto, Document aDocument) {

    final Optional<RbbFilmInfoDto> filmInfo = filmDetailDeserializer.deserialize(aUrlDto, aDocument);
    if (filmInfo.isPresent()) {
      RbbFilmInfoDto filmInfoDto = filmInfo.get();
      try {
        final ArdVideoInfoDTO videoInfo
            = gson.fromJson(new InputStreamReader(new URL(filmInfoDto.getUrl()).openStream(), StandardCharsets.UTF_8),
            ArdVideoInfoDTO.class);

        Film film = createFilm(filmInfoDto, videoInfo);
        taskResults.add(film);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();

      } catch (IOException e) {
        LOG.error("RbbFilmTask: error reading video infos " + filmInfoDto.getUrl(), e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }

    } else {
      LOG.error("RbbFilmTask: no film found for url " + aUrlDto.getUrl());
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new RbbFilmTask(crawler, aElementsToProcess);
  }

  private Film createFilm(final RbbFilmInfoDto aFilmInfoDto, final ArdVideoInfoDTO aVideoInfoDto) throws MalformedURLException {
    final Film film = new Film(UUID.randomUUID(), Sender.RBB, aFilmInfoDto.getTitle(),
        aFilmInfoDto.getTopic(), aFilmInfoDto.getTime(), aFilmInfoDto.getDuration());

    film.setBeschreibung(aFilmInfoDto.getDescription());
    film.setGeoLocations(CrawlerTool.getGeoLocations(Sender.RBB, aVideoInfoDto.getDefaultVideoUrl()));
    film.setWebsite(new URL(aFilmInfoDto.getWebsite()));
    if (StringUtils.isNotBlank(aVideoInfoDto.getSubtitleUrl())) {
      film.addSubtitle(new URL(aVideoInfoDto.getSubtitleUrl()));
    }
    addUrls(film, aVideoInfoDto.getVideoUrls());
    return film;
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
      throws MalformedURLException {
    for (final Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
    }
  }
}
