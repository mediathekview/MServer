package de.mediathekview.mserver.crawler.orf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfEpisodeInfoDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.crawler.orf.parser.OrfPlaylistDeserializer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class OrfFilmDetailTask extends AbstractDocumentTask<Film, TopicUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(OrfFilmDetailTask.class);

  private static final String TITLE_SELECTOR = ".description-title";
  private static final String VIDEO_META_DATA_SELECTOR = ".video-meta-data";
  private static final String TIME_SELECTOR = VIDEO_META_DATA_SELECTOR + "  time";
  private static final String DURATION_SELECTOR = VIDEO_META_DATA_SELECTOR + " span.duration";
  private static final String DESCRIPTION_SELECTOR = ".description-text";
  private static final String VIDEO_SELECTOR = "div.jsb_VideoPlaylist";

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_DATA_JSB = "data-jsb";

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final Type LIST_EPISODEINFO_TYPE_TOKEN =
      new TypeToken<List<OrfEpisodeInfoDTO>>() {}.getType();

  public OrfFilmDetailTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDto, Document aDocument) {
    final Optional<String> title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR, aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description =
        HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR, aDocument);

    final List<OrfEpisodeInfoDTO> episodes = parseEpisodes(aDocument);

    for (int i = 0; i < episodes.size(); i++) {
      OrfEpisodeInfoDTO episode = episodes.get(i);
      if (i == 0) {
        createFilm(aUrlDto, episode.getVideoInfo(), title, description, time, duration);
      } else {
        createFilm(
            aUrlDto,
            episode.getVideoInfo(),
            episode.getTitle(),
            episode.getDescription(),
            time,
            episode.getDuration());
      }
    }
  }

  @Override
  protected AbstractUrlTask<Film, TopicUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<TopicUrlDTO> aUrlsToCrawl) {
    return new OrfFilmDetailTask(crawler, aUrlsToCrawl);
  }

  private void createFilm(
      final TopicUrlDTO aUrlDto,
      final OrfVideoInfoDTO aVideoInfo,
      final Optional<String> aTitle,
      final Optional<String> aDescription,
      final Optional<LocalDateTime> aTime,
      final Optional<Duration> aDuration) {

    try {
      if (aTitle.isPresent()) {
        final Film film =
            new Film(
                UUID.randomUUID(),
                crawler.getSender(),
                aTitle.get(),
                aUrlDto.getTopic(),
                aTime.orElse(LocalDateTime.now()),
                aDuration.orElse(Duration.ZERO));

        film.setWebsite(new URL(aUrlDto.getUrl()));
        aDescription.ifPresent(film::setBeschreibung);

        if (StringUtils.isNotBlank(aVideoInfo.getSubtitleUrl())) {
          film.addSubtitle(new URL(aVideoInfo.getSubtitleUrl()));
        }

        addUrls(film, aVideoInfo.getVideoUrls());

        setGeoLocations(aVideoInfo, film);

        taskResults.add(film);
        crawler.incrementAndGetActualCount();
        crawler.updateProgress();
      } else {
        LOG.error("OrfFilmDetailTask: no title or video found for url " + aUrlDto.getUrl());
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

  private void setGeoLocations(OrfVideoInfoDTO aVideoInfo, Film film) {
    final List<GeoLocations> geoLocations = new ArrayList<>();
    if (aVideoInfo.getDefaultVideoUrl().contains("cms-austria")) {
      geoLocations.add(GeoLocations.GEO_AT);
    } else {
      geoLocations.add(GeoLocations.GEO_NONE);
    }
    film.setGeoLocations(geoLocations);
  }

  private void addUrls(final Film aFilm, final Map<Resolution, String> aVideoUrls)
      throws MalformedURLException {

    for (final Map.Entry<Resolution, String> qualitiesEntry : aVideoUrls.entrySet()) {
      aFilm.addUrl(qualitiesEntry.getKey(), CrawlerTool.stringToFilmUrl(qualitiesEntry.getValue()));
    }
  }

  private List<OrfEpisodeInfoDTO> parseEpisodes(Document aDocument) {
    Optional<String> json =
        HtmlDocumentUtils.getElementAttributeString(VIDEO_SELECTOR, ATTRIBUTE_DATA_JSB, aDocument);

    if (json.isPresent()) {

      final Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(LIST_EPISODEINFO_TYPE_TOKEN, new OrfPlaylistDeserializer())
              .create();

      return gson.fromJson(json.get(), LIST_EPISODEINFO_TYPE_TOKEN);
    }

    return new ArrayList<>();
  }

  private static Optional<LocalDateTime> parseDate(Document aDocument) {
    Optional<String> date =
        HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_DATETIME, aDocument);
    if (date.isPresent()) {
      String dateValue = date.get().replace("CET", " ").replace("CEST", " ");
      try {
        LocalDateTime localDate = LocalDateTime.parse(dateValue, DATE_TIME_FORMATTER);
        return Optional.of(localDate);
      } catch (DateTimeParseException e) {
        LOG.debug("OrfFilmDetailTask: unknown date format: " + date.get());
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(Document aDocument) {
    Optional<String> duration = HtmlDocumentUtils.getElementString(DURATION_SELECTOR, aDocument);
    if (!duration.isPresent()) {
      return Optional.empty();
    }

    Optional<ChronoUnit> unit = determineChronoUnit(duration.get());
    if (!unit.isPresent()) {
      LOG.debug("OrfFilmDetailTask: unknown duration type: " + duration.get());
      return Optional.empty();
    }

    String[] parts = duration.get().split(" ")[0].trim().split(":");
    if (parts.length != 2) {
      LOG.debug("OrfFilmDetailTask: unknown duration part count: " + duration.get());
      return Optional.empty();
    }

    ChronoUnit unitValue = unit.get();
    if (unitValue == ChronoUnit.MINUTES) {
      return Optional.of(
          Duration.ofMinutes(Long.parseLong(parts[0])).plusSeconds(Long.parseLong(parts[1])));
    }
    if (unitValue == ChronoUnit.HOURS) {
      return Optional.of(
          Duration.ofHours(Long.parseLong(parts[0])).plusMinutes(Long.parseLong(parts[1])));
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
