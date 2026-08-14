package mServer.crawler.sender.kika.tasks;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdUrlOptimizer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.kika.KikaAssetDto;
import mServer.crawler.sender.kika.KikaConstants;
import mServer.crawler.sender.kika.KikaFilmDto;
import mServer.crawler.sender.zdf.ZdfVideoUrlOptimizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class KikaConverterTask extends AbstractRecursivConverterTask<DatenFilm, KikaFilmDto> {
  private static final long serialVersionUID = 2437071037753218820L;
  private static final Logger LOG = LogManager.getLogger(KikaConverterTask.class);
  private final transient ZdfVideoUrlOptimizer zdfVideoUrlOptimizer;
  private final transient ArdUrlOptimizer ardUrlOptimizer;

  public KikaConverterTask(MediathekReader aCrawler, ConcurrentLinkedQueue<KikaFilmDto> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    zdfVideoUrlOptimizer = new ZdfVideoUrlOptimizer();
    ardUrlOptimizer = new ArdUrlOptimizer();
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, KikaFilmDto> createNewOwnInstance(
      ConcurrentLinkedQueue<KikaFilmDto> aElementsToProcess) {
    return new KikaConverterTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return 50;
  }

  @Override
  protected void processElement(KikaFilmDto aElement) {
    if (aElement.getAssets().isEmpty()) {
      LOG.error("No VideoUrls for {}", aElement.getUrl());
      Log.errorLog(374323228, "no videourls for " + aElement.getUrl());
      FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifier(), RunSender.Count.FEHLER);
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
      Log.errorLog(374323229, "missing title, topic, date or duration " + aElement.getUrl());
      FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifier(), RunSender.Count.FEHLER);
      return;
    }

    final Map<Qualities, String> videoUrls = getVideoUrls(aElement);
        
    String defaultUrl = Stream.of(Qualities.NORMAL, Qualities.HD, Qualities.SMALL)
        .map(videoUrls::get)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
    
    DatenFilm aFilm = new DatenFilm(
            Const.KIKA,
            aElement.getBroadcastSeriesTitle().get(),
            getWebsite(aElement).orElse(""),
            aElement.getTitle().get().replace("DGS", "Gebärdensprache"),
            defaultUrl,
            "",
            airedDate.get().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            airedDate.get().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            parseDuration(aElement, aElement.getDurationInSeconds()).get().getSeconds(),
            aElement.getDescription().orElse("")
    );
    //
    final String sub = getSubtitle(aElement);
    if (!sub.isEmpty()) {
      CrawlerTool.addUrlSubtitle(aFilm, sub);
    }
    //
    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(aFilm, videoUrls.get(Qualities.SMALL));
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      String url = videoUrls.get(Qualities.HD);
      if (!videoUrls.get(Qualities.HD).contains("wdrmedien")) {
        url = ardUrlOptimizer.optimizeHdUrl(url);
        url = zdfVideoUrlOptimizer.getOptimizedUrlHd(url);
      }
      CrawlerTool.addUrlHd(aFilm, url);
    }
    //
    getGeo(aElement).ifPresent(geos -> {
              geos.forEach(geo -> {
                aFilm.arr[DatenFilm.FILM_GEO] = geo.getDescription();
              });
            });

    if (!taskResults.add(aFilm)) {
      //LOG.debug("Rejected duplicate {}",aFilm);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getRunIdentifier(), RunSender.Count.FEHLER);
    }
  }
  //
  protected Map<Qualities,String> getVideoUrls(KikaFilmDto aElement) {
    Map<Qualities,String> urls = new EnumMap<>(Qualities.class);
    for (KikaAssetDto element : aElement.getAssets()) {
      if (element.getUrl().isPresent() && element.getResolution().isPresent()) {
        String url = element.getUrl().get();
        Qualities res = element.getResolution().get();
        if (Qualities.HD.equals(res) && !url.contains("wdrmedien")) {
          url = ardUrlOptimizer.optimizeHdUrl(url);
          url = zdfVideoUrlOptimizer.getOptimizedUrlHd(url);
        }
        urls.put(res, url);
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
  
  protected String getSubtitle(KikaFilmDto aElement) {
    KikaAssetDto subForAllAssets = aElement.getAssets().getFirst();
  if (subForAllAssets.getVideoSubtitle().isPresent()) {
    return UrlUtils.addProtocolIfMissing(subForAllAssets.getVideoSubtitle().get(), UrlUtils.PROTOCOL_HTTPS);
    }
  return "";
  }
  //
  protected Optional<String> getWebsite(KikaFilmDto aDTO) {
  Optional<String> rs = Optional.empty();
  if (aDTO.getUrlPath().isPresent()) {
      rs = Optional.of(KikaConstants.HOST + aDTO.getUrlPath().get() + "/" + aDTO.getApiId().get());
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
