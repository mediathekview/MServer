package de.mediathekview.mserver.crawler.orf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.crawler.orf.parser.OrfVideoDetailDeserializer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class OrfFilmDetailTask extends AbstractDocumentTask<Film, OrfTopicUrlDTO> {
  
  private static final Logger LOG = LogManager.getLogger(OrfFilmDetailTask.class);
  
  private static final String TITLE_SELECTOR = "h3.video_headline";
  private static final String BROADCAST_SELECTOR = "div.broadcast_information";
  private static final String TIME_SELECTOR = BROADCAST_SELECTOR + " > time";
  private static final String DURATION_SELECTOR = BROADCAST_SELECTOR + " > span.meta_duration";
  private static final String DESCRIPTION_SELECTOR = "div.details_description";
  private static final String VIDEO_SELECTOR = "div.jsb_VideoPlaylist";
  
  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_DATA_JSB = "data-jsb";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  
  private static final Type OPTIONAL_VIDEOINFO_TYPE_TOKEN = new TypeToken<Optional<OrfVideoInfoDTO>>() {}.getType();
  
  public OrfFilmDetailTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<OrfTopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }
  
  @Override
  protected void processDocument(OrfTopicUrlDTO aUrlDTO, Document aDocument) {
    final Optional<String> title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR, aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description = HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR, aDocument);
    
    final Optional<OrfVideoInfoDTO> videoInfoOptional = parseUrls(aDocument);
    
    try {
      if (videoInfoOptional.isPresent() && title.isPresent()) {
        final Film film = new Film(UUID.randomUUID(), crawler.getSender(), title.get(),
          aUrlDTO.getTheme(), time.orElse(LocalDateTime.now()), duration.orElse(Duration.ZERO));

        film.setWebsite(new URL(aUrlDTO.getUrl()));
        if (description.isPresent()) {
          film.setBeschreibung(description.get());
        }
     
        OrfVideoInfoDTO videoInfo = videoInfoOptional.get();
        if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
          film.addSubtitle(new URL(videoInfo.getSubtitleUrl()));
        }
        
        addUrls(film, videoInfo.getVideoUrls());

        // TODO geo

        taskResults.add(film);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } else {
        LOG.error("OrfFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (MalformedURLException ex) {
      LOG.fatal("A ORF URL can't be parsed.", ex);
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }    
  }

  @Override
  protected AbstractUrlTask<Film, OrfTopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<OrfTopicUrlDTO> aURLsToCrawl) {
    return new OrfFilmDetailTask(crawler, aURLsToCrawl);  
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
    throws MalformedURLException {
    
    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
    }
  }
  
  private Optional<OrfVideoInfoDTO> parseUrls(Document aDocument) {
    Optional<String> json = HtmlDocumentUtils.getElementAttributeString(VIDEO_SELECTOR, ATTRIBUTE_DATA_JSB, aDocument);
    
    if (json.isPresent()) {
    
      final Gson gson = new GsonBuilder().registerTypeAdapter(OPTIONAL_VIDEOINFO_TYPE_TOKEN,
          new OrfVideoDetailDeserializer()).create();
      
      return gson.fromJson(json.get(), OPTIONAL_VIDEOINFO_TYPE_TOKEN);
    }
    
    return Optional.empty();
  }
  
  private static Optional<LocalDateTime> parseDate(Document aDocument) {
    Optional<String> date = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_DATETIME, aDocument);
    if (date.isPresent()) {
      String dateValue = date.get().replace("CET", " ").replace("CEST", " ");
      try {
        LocalDateTime localDate = LocalDateTime.parse(dateValue, DATE_TIME_FORMATTER);
        return Optional.of(localDate);
      } catch(DateTimeParseException e) {
        LOG.debug("OrfFilmDetailTask: unknown date format: " + date.get());
      }
    }
    
    return Optional.empty();
  }
    
  private static Optional<Duration> parseDuration(Document aDocument) {
    Optional<String> duration = HtmlDocumentUtils.getElementString(DURATION_SELECTOR, aDocument);
    if (duration.isPresent()) {
      Optional<ChronoUnit> unit = determineChronoUnit(duration.get());
      if (unit.isPresent()) {
        String[] parts = duration.get().split(" ")[0].trim().split(":");
        if (parts.length == 2) {
          ChronoUnit unitValue = unit.get();
          if (unitValue == ChronoUnit.MINUTES) {
            return Optional.of(
              Duration.ofMinutes(Long.parseLong(parts[0]))
                .plusSeconds(Long.parseLong(parts[1]))
            );
          }
          if (unitValue == ChronoUnit.HOURS) {
            return Optional.of(
              Duration.ofHours(Long.parseLong(parts[0]))
                .plusMinutes(Long.parseLong(parts[1]))
            );
          }
        } else {
          LOG.debug("OrfFilmDetailTask: unknown duration type: " + duration.get());
        }
      } else {
        LOG.debug("OrfFilmDetailTask: unknown duration part count: " + duration.get());
      }
    }
    
    return Optional.empty();
  }  
  
  private static Optional<ChronoUnit> determineChronoUnit(String aDuration) {
    if (aDuration.contains("Min.")) {
      return Optional.of(ChronoUnit.MINUTES);
    }
    if (aDuration.contains("Std.")) {
      return Optional.of(ChronoUnit.HOURS);
    }
    
    return Optional.empty();
  }
}