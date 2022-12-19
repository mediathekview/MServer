package mServer.crawler.sender.kika.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.br.Resolution;
import mServer.crawler.sender.kika.KikaApiFilmDto;
import mServer.crawler.sender.kika.KikaApiVideoInfoDto;
import mServer.crawler.sender.kika.json.KikaApiVideoInfoPageDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiFilmTask extends AbstractJsonRestTask<DatenFilm, KikaApiVideoInfoDto, KikaApiFilmDto> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiFilmTask.class);

  public KikaApiFilmTask(MediathekReader crawler, ConcurrentLinkedQueue<KikaApiFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
  }

  @Override
  protected JsonDeserializer<KikaApiVideoInfoDto> getParser(KikaApiFilmDto aDTO) {
    return new KikaApiVideoInfoPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiVideoInfoDto>>() {
    }.getType();
  }

  @Override
  protected void handleHttpError(KikaApiFilmDto dto, URI url, Response response) {
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
    Log.errorLog(324978334, String.format("A HTTP error %d occurred when getting REST information from: \"%s\".",
            response.getStatus(),
            url));
  }

  @Override
  protected void postProcessing(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    //
    if (aResponseObj.getErrorCode().isPresent()) {
      LOG.error("Error {} : {} for target {} ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().orElse(""), aDTO.getUrl());
      Log.errorLog(324978335, String.format("Error %s} : %s for target %s ", aResponseObj.getErrorCode().get(), aResponseObj.getErrorMesssage().orElse(""), aDTO.getUrl()));
      return;
    }
    //
    if (aResponseObj.getVideoUrls().isEmpty()) {
      LOG.error("No VideoUrls for {}", aDTO.getUrl());
      return;
    }
    //
    final Optional<LocalDateTime> airedDate = getAiredDateTime(aDTO);
    if (!aDTO.getTitle().isPresent() || !aDTO.getTopic().isPresent() || !airedDate.isPresent() || !aDTO.getDuration().isPresent()) {
      if (!aDTO.getTitle().isPresent()) {
        LOG.error("Missing title for {}", aDTO.getUrl());
      } else if (!aDTO.getTopic().isPresent()) {
        LOG.error("Missing topic for {}", aDTO.getUrl());
      } else if (!airedDate.isPresent()) {
        LOG.error("Missing date for {}", aDTO.getUrl());
      } else if (!aDTO.getDuration().isPresent()) {
        LOG.error("Missing duration for {}", aDTO.getUrl());
      }
      return;
    }
    //
    Map<Resolution, String> videoUrls = getVideoUrls(aResponseObj, aDTO);
    Set<URL> subs = getSubtitle(aResponseObj, aDTO);

    DatenFilm aFilm = new DatenFilm(
        Const.KIKA,
        aDTO.getTopic().get(),
        aDTO.getWebsite().orElse(""),
        aDTO.getTitle().get(),
        videoUrls.get(Resolution.NORMAL),
        ((subs.isEmpty()) ? "" : subs.toArray()[0].toString()),
        airedDate.get().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
        airedDate.get().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
        parseDuration(aDTO, aDTO.getDuration()).get().getSeconds(),
        aDTO.getDescription().orElse("")
     );
    //
    if (videoUrls.containsKey(Resolution.SMALL)) {
      CrawlerTool.addUrlKlein(aFilm, videoUrls.get(Resolution.SMALL));
    }
    if (videoUrls.containsKey(Resolution.HD)) {
      CrawlerTool.addUrlHd(aFilm, videoUrls.get(Resolution.HD));
    }
    //
    getGeo(aDTO).ifPresent(geos -> {
      geos.forEach(geo -> {
        aFilm.arr[DatenFilm.FILM_GEO] = geo.getDescription();
      });
    });
    //
    taskResults.add(aFilm);
  }


  protected Optional<LocalDateTime> getAiredDateTime(KikaApiFilmDto aDTO) {
    Optional<LocalDateTime> airedDate;
    if (aDTO.getDate().isPresent()) {
      airedDate = parseLocalDateTime(aDTO, aDTO.getDate());
    } else {
      airedDate = parseLocalDateTime(aDTO, aDTO.getAppearDate());
    }
    return airedDate;
  }

  protected Set<URL> getSubtitle(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    Set<URL> urls = new HashSet<>();
    if (aResponseObj.hasSubtitle()) {
      for (String subtitleUrlAsString : aResponseObj.getSubtitle()) {
        try {
          urls.add(new URL(UrlUtils.addProtocolIfMissing(subtitleUrlAsString, UrlUtils.PROTOCOL_HTTPS)));
        } catch (MalformedURLException e) {
          LOG.error("Invalid subtitle url {} for {} error {}", subtitleUrlAsString, aDTO.getUrl(), e);
        }
      }
      if (aResponseObj.getSubtitle().isEmpty()) {
        LOG.error("Missing subtitle for {}", aDTO.getUrl());
      }
    }
    return urls;
  }

  protected Map<Resolution, String> getVideoUrls(KikaApiVideoInfoDto aResponseObj, KikaApiFilmDto aDTO) {
    Map<Resolution, String> urls = new EnumMap<>(Resolution.class);
    for (Map.Entry<Resolution, String> element : aResponseObj.getVideoUrls().entrySet()) {
      urls.put(element.getKey(), element.getValue());
    }
    return urls;
  }

  protected Optional<URL> getWebsite(KikaApiFilmDto aDTO) {
    Optional<URL> rs = Optional.empty();
    if (aDTO.getWebsite().isPresent()) {
      try {
        rs = Optional.of(new URL(aDTO.getWebsite().get()));
      } catch (MalformedURLException e) {
        LOG.error("Invalid website url {} for {} error {}", aDTO.getWebsite().get(), aDTO.getUrl(), e);
      }
    }
    return rs;
  }

  protected Optional<Collection<GeoLocations>> getGeo(KikaApiFilmDto aDTO) {
    Optional<Collection<GeoLocations>> rs = Optional.empty();
    if (aDTO.getGeoProtection().isPresent()) {
      Optional<GeoLocations> geo = parseGeo(aDTO, aDTO.getGeoProtection());
      if (geo.isPresent()) {
        Collection<GeoLocations> collectionOfGeolocations = new ArrayList<>();
        collectionOfGeolocations.add(geo.get());
        rs = Optional.of(collectionOfGeolocations);
      }
    }
    return rs;
  }

  protected Optional<LocalDateTime> parseLocalDateTime(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<LocalDateTime> result = Optional.empty();
    if (text.isPresent()) {
      try {
        DateTimeFormatter formatter = DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        result = Optional.of(LocalDateTime.parse(text.get().substring(0, 19), formatter));
      } catch (Exception e) {
        LOG.error("DateTimeFormatter failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }

  //
  protected Optional<Duration> parseDuration(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<Duration> result = Optional.empty();
    if (text.isPresent()) {
      try {
        int min = Integer.parseInt(text.get());
        result = Optional.of(Duration.ofSeconds(min));
      } catch (Exception e) {
        LOG.error("Parse duration failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }

  //
  protected Optional<GeoLocations> parseGeo(KikaApiFilmDto sourceUrl, Optional<String> text) {
    Optional<GeoLocations> result = Optional.empty();
    if (text.isPresent()) {
      if (text.get().equalsIgnoreCase("germany")) {
        return Optional.of(GeoLocations.GEO_DE);
      } else if (text.get().equalsIgnoreCase("worldwide")) {
        return Optional.empty();
      } else {
        LOG.error("Unknow GeoLocations {} url {}", text.get(), sourceUrl.getUrl());
      }
    }
    return result;
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, KikaApiFilmDto> createNewOwnInstance(
          ConcurrentLinkedQueue<KikaApiFilmDto> aElementsToProcess) {
    return new KikaApiFilmTask(crawler, aElementsToProcess);
  }


}
