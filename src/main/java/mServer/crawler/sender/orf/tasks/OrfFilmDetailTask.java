package mServer.crawler.sender.orf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
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
import mServer.crawler.sender.orf.parser.OrfPlaylistDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public class OrfFilmDetailTask extends AbstractDocumentTask<DatenFilm, TopicUrlDTO> {

  private static final String TITLE_SELECTOR = "h3.video_headline";
  private static final String BROADCAST_SELECTOR = "div.broadcast_information";
  private static final String TIME_SELECTOR = BROADCAST_SELECTOR + " > time";
  private static final String DURATION_SELECTOR = BROADCAST_SELECTOR + " > span.meta_duration";
  private static final String DESCRIPTION_SELECTOR = "div.details_description";
  private static final String VIDEO_SELECTOR = "div.jsb_VideoPlaylist";

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_DATA_JSB = "data-jsb";

  private static final DateTimeFormatter DATE_TIME_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Type LIST_EPISODEINFO_TYPE_TOKEN = new TypeToken<List<OrfEpisodeInfoDTO>>() {
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

    final List<OrfEpisodeInfoDTO> episodes = parseEpisodes(aDocument);

    for (int i = 0; i < episodes.size(); i++) {
      OrfEpisodeInfoDTO episode = episodes.get(i);
      if (i == 0) {
        createFilm(aUrlDTO, episode.getVideoInfo(), title, description, time, duration);
      } else {
        createFilm(aUrlDTO, episode.getVideoInfo(), episode.getTitle(), episode.getDescription(), time, episode.getDuration());
      }
    }
  }

  @Override
  protected AbstractUrlTask<DatenFilm, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl) {
    return new OrfFilmDetailTask(crawler, aURLsToCrawl);
  }

  private void createFilm(final TopicUrlDTO aUrlDTO,
          final OrfVideoInfoDTO aVideoInfo,
          final Optional<String> aTitle,
          final Optional<String> aDescription,
          final Optional<LocalDateTime> aTime,
          final Optional<Duration> aDuration) {

    try {
      if (aTitle.isPresent()) {
        LocalDateTime time = aTime.orElse(LocalDateTime.now());

        String datum = time.format(DATE_FORMAT);
        String zeit = time.format(TIME_FORMAT);
        String url = aVideoInfo.getDefaultVideoUrl();

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

        if (StringUtils.isNotBlank(aVideoInfo.getSubtitleUrl())) {
          CrawlerTool.addUrlSubtitle(film, aVideoInfo.getSubtitleUrl());
        }

        addUrls(film, aVideoInfo.getVideoUrls());

        taskResults.add(film);
      } else {
        Log.sysLog("OrfFilmDetailTask: no title or video found for url " + aUrlDTO.getUrl());
      }
    } catch (MalformedURLException ex) {
      Log.errorLog(984514561, ex);
    }
  }

  private void addUrls(final DatenFilm aFilm, final Map<Qualities, String> aVideoUrls)
          throws MalformedURLException {

    if (aVideoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(aFilm, aVideoUrls.get(Qualities.HD), "");
    }
    if (aVideoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(aFilm, aVideoUrls.get(Qualities.SMALL), "");
    }
  }

  private List<OrfEpisodeInfoDTO> parseEpisodes(Document aDocument) {
    Optional<String> json = HtmlDocumentUtils.getElementAttributeString(VIDEO_SELECTOR, ATTRIBUTE_DATA_JSB, aDocument);

    if (json.isPresent()) {

      final Gson gson = new GsonBuilder().registerTypeAdapter(LIST_EPISODEINFO_TYPE_TOKEN,
              new OrfPlaylistDeserializer()).create();

      return gson.fromJson(json.get(), LIST_EPISODEINFO_TYPE_TOKEN);
    }

    return new ArrayList<>();
  }

  private static Optional<LocalDateTime> parseDate(Document aDocument) {
    Optional<String> date = HtmlDocumentUtils.getElementAttributeString(TIME_SELECTOR, ATTRIBUTE_DATETIME, aDocument);
    if (date.isPresent()) {
      String dateValue = date.get().replace("CET", " ").replace("CEST", " ");
      try {
        LocalDateTime localDate = LocalDateTime.parse(dateValue, DATE_TIME_FORMATTER);
        return Optional.of(localDate);
      } catch (DateTimeParseException e) {
        Log.sysLog("OrfFilmDetailTask: unknown date format: " + date.get());
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
      Log.sysLog("OrfFilmDetailTask: unknown duration type: " + duration.get());
      return Optional.empty();
    }

    String[] parts = duration.get().split(" ")[0].trim().split(":");
    if (parts.length != 2) {
      Log.sysLog("OrfFilmDetailTask: unknown duration part count: " + duration.get());
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
