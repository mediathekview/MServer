package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoPageDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiFilmTask extends AbstractJsonRestTask<Film, KikaApiVideoInfoDto, KikaApiFilmDto> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiFilmTask.class);

  public KikaApiFilmTask(AbstractCrawler crawler, Queue<KikaApiFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
  }

  @Override
  protected JsonDeserializer<KikaApiVideoInfoDto> getParser(KikaApiFilmDto aDTO) {
    return new KikaApiVideoInfoPageDeserializer(crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiVideoInfoDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    
  }

  @Override
  protected void postProcessing(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().get(), aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    if (aResponseObj.getVideoUrls().size() == 0) {
      LOG.error("No VideoUrls for {}", aDTO.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    //
    Film aFilm = new Film(
        UUID.randomUUID(),
        crawler.getSender(),
        aDTO.getTitle().get(),
        aDTO.getTopic().get(),
        aDTO.getDate().get(),
        aDTO.getDuration().get()
        );
    if (aDTO.getDescription().isPresent()) {
      aFilm.setBeschreibung(aDTO.getDescription().get());
    }
    if (aDTO.getGeoProtection().isPresent()) {
      Collection<GeoLocations> collectionOfGeolocations = new ArrayList<GeoLocations>();
      collectionOfGeolocations.add(aDTO.getGeoProtection().get());
      aFilm.setGeoLocations(collectionOfGeolocations);
    }
    if (aDTO.getWebsite().isPresent()) {
      try {
        aFilm.setWebsite(new URL(aDTO.getWebsite().get()));
      } catch (MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    //
    for (Map.Entry<Resolution,FilmUrl> element : aResponseObj.getVideoUrls().entrySet()) {
      aFilm.addUrl(element.getKey(), element.getValue());
    }
    //
    for (URL element : aResponseObj.getSubtitle()) {
      aFilm.addSubtitle(element);
    }
    taskResults.add(aFilm);
    crawler.incrementAndGetActualCount();
    crawler.updateProgress();
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, KikaApiFilmDto> createNewOwnInstance(
      Queue<KikaApiFilmDto> aElementsToProcess) {
    return new KikaApiFilmTask(crawler, aElementsToProcess);
  }

  
}
