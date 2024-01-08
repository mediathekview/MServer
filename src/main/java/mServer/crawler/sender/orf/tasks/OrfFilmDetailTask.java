package mServer.crawler.sender.orf.tasks;

import mServer.crawler.sender.base.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;

import java.io.IOException;
import java.lang.reflect.Type;
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
import mServer.crawler.sender.orf.OrfEpisodeInfoDTO;
import mServer.crawler.sender.orf.OrfVideoInfoDTO;
import mServer.crawler.sender.orf.TopicUrlDTO;
import mServer.crawler.sender.orf.json.OrfMoreEpisodesDeserializer;
import mServer.crawler.sender.orf.parser.OrfMoreEpisodesParser;
import mServer.crawler.sender.orf.parser.OrfPlaylistDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

public class OrfFilmDetailTask extends OrfTaskBase<DatenFilm, TopicUrlDTO> {

  private static final String TITLE_SELECTOR = ".description-container .description-title";
  private static final String VIDEO_META_DATA_SELECTOR = ".video-meta-data";
  private static final String TIME_SELECTOR = VIDEO_META_DATA_SELECTOR + "  time";
  private static final String DURATION_SELECTOR = VIDEO_META_DATA_SELECTOR + " span.duration";
  private static final String DESCRIPTION_SELECTOR = ".description-container .description-text";
  private static final String VIDEO_SELECTOR = "div.jsb_VideoPlaylist";
  private static final String MORE_EPISODES_SELECTOR = "div.more-episodes";

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_DATA_JSB = "data-jsb";

  private static final String PREFIX_AUDIO_DESCRIPTION = "AD |";

  private static final DateTimeFormatter DATE_TIME_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Type CRAWLER_URL_TYPE_TOKEN = new TypeToken<CrawlerUrlDTO>() {}.getType();
  private static final Type LIST_EPISODEINFO_TYPE_TOKEN = new TypeToken<List<OrfEpisodeInfoDTO>>() {
  }.getType();

  private final boolean processMoreEpisodes;
  private final transient JsoupConnection jsoupConnection;

  public OrfFilmDetailTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDTOs, boolean processMoreEpisodes) {
    super(aCrawler, aUrlToCrawlDTOs);
    this.processMoreEpisodes = processMoreEpisodes;
    jsoupConnection = new JsoupConnection();
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

    if (processMoreEpisodes) {
      final List<TopicUrlDTO> topicUrlDTOS = parseMoreEpisodes(aDocument, aUrlDTO.getTopic());
      topicUrlDTOS.remove(aUrlDTO);
      processMoreEpisodes(topicUrlDTOS);
    }

    ORF_LOGGER.trace(String.format("%s - %s: Anzahl Filme: %d", aUrlDTO.getTopic(), aUrlDTO.getUrl(), taskResults.size()));
  }

  @Override
  protected AbstractUrlTask<DatenFilm, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aURLsToCrawl) {
    return createNewOwnInstance(aURLsToCrawl, processMoreEpisodes);
  }

  private AbstractUrlTask<DatenFilm, TopicUrlDTO> createNewOwnInstance(final ConcurrentLinkedQueue<TopicUrlDTO> urlsToCrawl, boolean processMoreEpisodes) {
    return new OrfFilmDetailTask(crawler, urlsToCrawl, processMoreEpisodes);
  }

  private void createFilm(final TopicUrlDTO aUrlDTO,
          final OrfVideoInfoDTO aVideoInfo,
          final Optional<String> aTitle,
          final Optional<String> aDescription,
          final Optional<LocalDateTime> aTime,
          final Optional<Duration> aDuration) {

    if (aTitle.isPresent()) {
      boolean isAudioDescription = aUrlDTO.getTopic().startsWith(PREFIX_AUDIO_DESCRIPTION);

      LocalDateTime time = aTime.orElse(LocalDateTime.now());

      String datum = time.format(DATE_FORMAT);
      String zeit = time.format(TIME_FORMAT);
      String url = aVideoInfo.getDefaultVideoUrl();

      final DatenFilm film = new DatenFilm(crawler.getSendername(),
              isAudioDescription
                      ? trimAudioDescriptionPrefix(aUrlDTO.getTopic())
                      : aUrlDTO.getTopic(),
              aUrlDTO.getUrl(),
              isAudioDescription
                      ? trimAudioDescriptionPrefix(aTitle.get()) + " (Audiodeskription)"
                      : aTitle.get(),
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
  }

  private String trimAudioDescriptionPrefix(String text) {
    return text.substring(PREFIX_AUDIO_DESCRIPTION.length());
  }

  private void addUrls(final DatenFilm aFilm, final Map<Qualities, String> aVideoUrls) {

    if (aVideoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(aFilm, aVideoUrls.get(Qualities.HD));
    }
    if (aVideoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(aFilm, aVideoUrls.get(Qualities.SMALL));
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
    if (unitValue == ChronoUnit.SECONDS || unitValue == ChronoUnit.MINUTES) {
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
    if (aDuration.contains("Sek.")) {
      return Optional.of(ChronoUnit.SECONDS);
    }

    return Optional.empty();
  }

  private List<TopicUrlDTO> parseMoreEpisodes(final Document document, final String topic) {
    final Optional<String> json = HtmlDocumentUtils.getElementAttributeString(MORE_EPISODES_SELECTOR, ATTRIBUTE_DATA_JSB, document);
    if (json.isPresent()) {
      final Gson gson =
              new GsonBuilder()
                      .registerTypeAdapter(CRAWLER_URL_TYPE_TOKEN, new OrfMoreEpisodesDeserializer())
                      .create();

      CrawlerUrlDTO moreEpisodesUrl = gson.fromJson(json.get(), CRAWLER_URL_TYPE_TOKEN);
      if (moreEpisodesUrl != null) {
        try {
          final Document moreEpisodesDocument = jsoupConnection.getDocument(moreEpisodesUrl.getUrl());
          OrfMoreEpisodesParser parser = new OrfMoreEpisodesParser();
          return parser.parse(moreEpisodesDocument, topic);
        } catch (IOException e) {
          Log.errorLog(237462889, String.format("OrfFilmDetailTask: loading more episodes url %s failed.", moreEpisodesUrl.getUrl()));
        }
      }
    }

    return new ArrayList<>();
  }

  private void processMoreEpisodes(final List<TopicUrlDTO> moreFilms) {
    if (moreFilms != null && !moreFilms.isEmpty()) {
      final ConcurrentLinkedQueue<TopicUrlDTO> queue = new ConcurrentLinkedQueue<>(moreFilms);
      final OrfFilmDetailTask task = (OrfFilmDetailTask) createNewOwnInstance(queue, false);
      task.fork();
      taskResults.addAll(task.join());
    }
  }
}
