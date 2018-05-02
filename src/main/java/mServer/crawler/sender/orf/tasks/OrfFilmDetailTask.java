package mServer.crawler.sender.orf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.orf.HtmlDocumentUtils;
import mServer.crawler.sender.orf.OrfEpisodeInfoDTO;
import mServer.crawler.sender.orf.OrfVideoInfoDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import mServer.crawler.sender.orf.parser.OrfEpisodeDeserializer;
import mServer.crawler.sender.orf.parser.OrfVideoDetailDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfFilmDetailTask extends AbstractDocumentTask<DatenFilm, TopicUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(OrfFilmDetailTask.class);

  private static final String TITLE_SELECTOR = "h3.video_headline";
  private static final String BROADCAST_SELECTOR = "div.broadcast_information";
  private static final String TIME_SELECTOR = BROADCAST_SELECTOR + " > time";
  private static final String DURATION_SELECTOR = BROADCAST_SELECTOR + " > span.meta_duration";
  private static final String DESCRIPTION_SELECTOR = "div.details_description";
  private static final String VIDEO_SELECTOR = "div.jsb_VideoPlaylist";
  private static final String EPISODE_SELECTOR = "li.jsb_PlaylistItemFullscreen";

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_DATA_JSB = "data-jsb";

  private static final DateTimeFormatter DATE_TIME_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Type OPTIONAL_VIDEOINFO_TYPE_TOKEN = new TypeToken<Optional<OrfVideoInfoDTO>>() {
  }.getType();
  private static final Type OPTIONAL_EPISODEINFO_TYPE_TOKEN = new TypeToken<Optional<OrfEpisodeInfoDTO>>() {
  }.getType();

  public OrfFilmDetailTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDTO, Document aDocument) {
    final Optional<String> title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR, aDocument);
    final Optional<LocalDateTime> time = parseDate(aDocument);
    final Optional<Duration> duration = parseDuration(aDocument);
    final Optional<String> description = HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR, aDocument);

    final Optional<OrfVideoInfoDTO> videoInfoOptional = parseUrls(aDocument);

    createFilm(aUrlDTO, videoInfoOptional, title, description, time, duration);

    final List<OrfEpisodeInfoDTO> episodes = parseEpisodes(aDocument);
    episodes.forEach(episode -> {
      createFilm(aUrlDTO, Optional.of(episode.getVideoInfo()), episode.getTitle(), episode.getDescription(), time, episode.getDuration());
    });
  }

  @Override
  protected AbstractUrlTask<DatenFilm, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl) {
    return new OrfFilmDetailTask(crawler, aURLsToCrawl);
  }

  private void createFilm(final TopicUrlDTO aUrlDTO,
          final Optional<OrfVideoInfoDTO> aVideoInfo,
          final Optional<String> aTitle,
          final Optional<String> aDescription,
          final Optional<LocalDateTime> aTime,
          final Optional<Duration> aDuration) {

    try {
      if (aVideoInfo.isPresent() && aTitle.isPresent()) {
        OrfVideoInfoDTO videoInfo = aVideoInfo.get();

        LocalDateTime time = aTime.orElse(LocalDateTime.now());

        String datum = time.format(DATE_FORMAT);
        String zeit = time.format(TIME_FORMAT);
        String url = videoInfo.getDefaultVideoUrl();

        final DatenFilm film = new DatenFilm(crawler.getSendername(),
                aUrlDTO.getTopic(),
                aUrlDTO.getUrl(),
                aTitle.get(),
                url,
                "",
                datum,
                zeit,
                aDuration.orElse(Duration.ZERO).getSeconds(),
                aDescription.orElse(""));

        if (StringUtils.isNotBlank(videoInfo.getSubtitleUrl())) {
          CrawlerTool.addUrlSubtitle(film, videoInfo.getSubtitleUrl());
        }

        addUrls(film, videoInfo.getVideoUrls());

// TODO Geo???        
        taskResults.add(film);
        //TODO
        //crawler.incrementAndGetActualCount();
        //crawler.updateProgress();
      } else {
        LOG.error("OrfFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
        //crawler.incrementAndGetErrorCount();
        //crawler.updateProgress();
      }
    } catch (MalformedURLException ex) {
      LOG.fatal("A ORF URL can't be parsed.", ex);
      //crawler.printErrorMessage();
      //crawler.incrementAndGetErrorCount();
      //crawler.updateProgress();
    }
  }

  private void addUrls(final DatenFilm aFilm, final Map<Qualities, String> aVideoUrls)
          throws MalformedURLException {

    if (aVideoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(aFilm, aVideoUrls.get(Qualities.HD), "");
    }
    if (aVideoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlHd(aFilm, aVideoUrls.get(Qualities.SMALL), "");
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

    return Optional.empty();
  }

  private static List<OrfEpisodeInfoDTO> parseEpisodes(final Document aDocument) {
    final List<OrfEpisodeInfoDTO> episodes = new ArrayList<>();

    Elements elements = aDocument.select(EPISODE_SELECTOR);
    elements.forEach(element -> {
      String json = element.attr(ATTRIBUTE_DATA_JSB);

      if (!json.isEmpty()) {

        final Gson gson = new GsonBuilder().registerTypeAdapter(OPTIONAL_EPISODEINFO_TYPE_TOKEN,
                new OrfEpisodeDeserializer()).create();

        Optional<OrfEpisodeInfoDTO> episode = gson.fromJson(json, OPTIONAL_EPISODEINFO_TYPE_TOKEN);
        if (episode.isPresent()) {
          episodes.add(episode.get());
        }
      }
    });

    return episodes;
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
