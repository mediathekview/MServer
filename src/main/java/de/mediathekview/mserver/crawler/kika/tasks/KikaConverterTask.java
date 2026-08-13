package de.mediathekview.mserver.crawler.kika.tasks;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.ard.ArdUrlOptimizer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.KikaAssetDto;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaFilmDto;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;

public class KikaConverterTask extends AbstractRecursiveConverterTask<Film, KikaFilmDto>  {
  private static final long serialVersionUID = 2437071037753218820L;
  private static final Logger LOG = LogManager.getLogger(KikaConverterTask.class);
  private ZdfVideoUrlOptimizer zdfVideoUrlOptimizer;
  private ArdUrlOptimizer ardUrlOptimizer;

  public KikaConverterTask(AbstractCrawler aCrawler, Queue<KikaFilmDto> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    zdfVideoUrlOptimizer = new ZdfVideoUrlOptimizer(crawler);
    ardUrlOptimizer = new ArdUrlOptimizer(crawler);
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, KikaFilmDto> createNewOwnInstance(
      Queue<KikaFilmDto> aElementsToProcess) {
    return new KikaConverterTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return config.getMaximumUrlsPerTask();
  }

  @Override
  protected void processElement(KikaFilmDto aElement) {
    if (aElement.getAssets().size() == 0) {
      LOG.error("No VideoUrls for {}", aElement.getUrl());
      crawler.incrementAndGetErrorCount();
      return;
    }
    final Optional<LocalDateTime> airedDate = getAiredDateTime(aElement);
    if (aElement.getTitle().isEmpty() || aElement.getBroadcastSeriesTitle().isEmpty() || aElement.getDate().isEmpty()) {
      if (aElement.getTitle().isEmpty()) {
        LOG.error("Missing title for {}", aElement.getUrl());
      } else if (aElement.getBroadcastSeriesTitle().isEmpty()) {
        LOG.error("Missing topic for {}", aElement.getUrl());
      } else if (airedDate.isEmpty()) {
        LOG.error("Missing date for {}", aElement.getUrl());
      } else if (aElement.getDurationInSeconds().isEmpty()) {
        LOG.error("Missing duration for {}", aElement.getUrl());
      }
      crawler.incrementAndGetErrorCount();
      return;
    }
    Film aFilm = new Film(
        UUID.randomUUID(),
        crawler.getSender(),
        aElement.getTitle().get(),
        aElement.getBroadcastSeriesTitle().get(),
        airedDate.get(),
        parseDuration(aElement, aElement.getDurationInSeconds()).get()
        );
    if (aElement.getDescription().isPresent()) {
      aFilm.setBeschreibung(aElement.getDescription().get());
    }
    aFilm.setId(aElement.getId().get());
    getWebsite(aElement).ifPresent(aFilm::setWebsite);
    // logic for AD and DGS
    if (aElement.getTitle().get().endsWith("(AD)")) {
      aFilm.setAudioDescriptions(getVideoUrls(aElement));
    } else if (aElement.getTitle().get().endsWith("(DGS)")) {
      aFilm.setSignLanguages(getVideoUrls(aElement));
    } else {
      aFilm.setUrls(getVideoUrls(aElement));
    }
    aFilm.addAllSubtitleUrls(getSubtitle(aElement));
    getGeo(aElement).ifPresent(aFilm::setGeoLocations);
    //
    if (!taskResults.add(aFilm)) {
      //LOG.debug("Rejected duplicate {}",aFilm);
      crawler.incrementAndGetErrorCount();
    } else {
      crawler.incrementAndGetActualCount();
    }
    crawler.updateProgress();
  }
  //
  protected Map<Resolution,FilmUrl> getVideoUrls(KikaFilmDto aElement) {
    Map<Resolution, FilmUrl> urls = new EnumMap<>(Resolution.class);
    for (KikaAssetDto element : aElement.getAssets()) {
      if (element.getUrl().isPresent() && element.getResolution().isPresent()) {
        try {
          String url = element.getUrl().get();
          Resolution res = element.getResolution().get();
          Long fs = element.getFileSize().orElse(0).longValue();
          if (Resolution.HD.equals(res) && !url.contains("wdrmedien")) {
            url = ardUrlOptimizer.optimizeHdUrl(url);
            url = zdfVideoUrlOptimizer.getOptimizedUrlHd(url);
          }
          if (fs == 0) {
            fs = crawler.determineFileSizeInKB(url);
          }
          final FilmUrl filmUrl = new FilmUrl(url, fs);
          urls.put(res, filmUrl);
        } catch (MalformedURLException e) {
          LOG.error("Invalid video url {} for {} error {}", element.getUrl().get(), aElement.getUrl(), e);
        }
      }
    }
    return urls;
  }
  //
  protected Optional<Collection<GeoLocations>> getGeo(KikaFilmDto aDTO) {
    Optional<Collection<GeoLocations>> rs = Optional.empty();
    if (!aDTO.getAssets().isEmpty()) {
      Optional<GeoLocations> geo = parseGeo(aDTO);
      if (geo.isPresent()) {
        Collection<GeoLocations> collectionOfGeolocations = new ArrayList<>();
        collectionOfGeolocations.add(geo.get());
        rs = Optional.of(collectionOfGeolocations);
      }
    }
    return rs;
  }
  protected Optional<GeoLocations> parseGeo(KikaFilmDto aDTO) {
    Optional<GeoLocations> result = Optional.empty();
    String text = aDTO.getAssets().getFirst().getUrl().get();
    if (text.contains("/kika_de/") || text.contains("/de/") || 
        text.contains("/content-de/") || text.contains("kika_de-prod/geo/") ||
        text.contains("/progressive_geo/")      
    ) {
      return Optional.of(GeoLocations.GEO_DE);
    } else if (text.contains("/de-at-ch/") || text.contains("/dach/")) {
      return Optional.of(GeoLocations.GEO_DE_AT_CH);
    } else if (text.contains("/none/") || text.contains("/weltweit/")) {
      return  Optional.empty();
    } else {
      //LOG.error("Unknow GeoLocations {} url {}", text, aDTO.getUrl());
    }
    
    return result;
  }
  //
  protected Optional<LocalDateTime> getAiredDateTime(KikaFilmDto aDTO) {
    Optional<LocalDateTime> airedDate;
    if (aDTO.getDate().isPresent()) {
      airedDate = parseLocalDateTime(aDTO, aDTO.getDate()); 
    } else {
      airedDate = parseLocalDateTime(aDTO, aDTO.getGenDate());
    }
    return airedDate;
  }
  
  protected Set<URL> getSubtitle(KikaFilmDto aElement) {
  Set<URL> urls = new HashSet<>();
  KikaAssetDto subForAllAssets = aElement.getAssets().getFirst();
  if (subForAllAssets.getVideoSubtitle().isPresent()) {
      try {
        urls.add(URI.create(UrlUtils.addProtocolIfMissing(subForAllAssets.getVideoSubtitle().get(), UrlUtils.PROTOCOL_HTTPS)).toURL());
      } catch (MalformedURLException e) {
        LOG.error("Invalid subtitle url {} for {} error {}", subForAllAssets.getVideoSubtitle().get(), aElement.getUrl(), e);
      }
      if (subForAllAssets.getVideoSubtitle().isEmpty()) {
        LOG.error("Missing subtitle for {}", aElement.getUrl());
      }
    }
  return urls;
  }
  //
  protected Optional<URL> getWebsite(KikaFilmDto aDTO) {
  Optional<URL> rs = Optional.empty();
  if (aDTO.getUrlPath().isPresent()) {
      try {
        rs = Optional.of(URI.create(KikaConstants.HOST + aDTO.getUrlPath().get() + "/" + aDTO.getApiId().get()).toURL());
      } catch (MalformedURLException e) {
        LOG.error("Invalid website url {} for {} error {}", KikaConstants.HOST + aDTO.getUrlPath().get(), aDTO.getUrl(), e);
      }
    }
  return rs;
  }
  //
  protected Optional<LocalDateTime> parseLocalDateTime(KikaFilmDto sourceUrl, Optional<String> text) {
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
  protected Optional<Duration> parseDuration(KikaFilmDto sourceUrl, Optional<String> text) {
    Optional<Duration> result = Optional.of(Duration.ZERO);
    if (text.isPresent()) {
      try {
        int sec = Integer.parseInt(text.get());
        result = Optional.of(Duration.ofSeconds(sec));
      } catch (Exception e) {
        LOG.error("Parse duration failed for string {} url {} exception {}", text.get(), sourceUrl.getUrl(), e);
      }
    }
    return result;
  }  

}
