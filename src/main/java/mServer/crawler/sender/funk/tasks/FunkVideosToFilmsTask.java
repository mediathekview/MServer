package mServer.crawler.sender.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.FilmUrlInfoDto;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.funk.FilmInfoDto;
import mServer.crawler.sender.funk.FunkChannelDTO;
import mServer.crawler.sender.funk.json.NexxCloudVideoDetailsDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FunkVideosToFilmsTask
        extends AbstractJsonRestTask<DatenFilm, Set<FilmUrlInfoDto>, FilmInfoDto> {
  private static final Logger LOG = LogManager.getLogger(FunkVideosToFilmsTask.class);
  private static final String POST_FORM_FIELD_ADD_STREAM_DETAILS = "addStreamDetails";
  private static final String POST_FORM_FIELD_VALUE_ONE = "1";
  private static final String HEADER_X_REQUEST_CID = "x-request-cid";
  private static final String HEADER_X_REQUEST_TOKEN = "x-request-token";
  private static final String REQUEST_TOKEN = "137782e774d7cadc93dcbffbbde0ce9c";

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private Long sessionId;
  private final transient Map<String, FunkChannelDTO> channels;

  public FunkVideosToFilmsTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<FilmInfoDto> filmInfos,
          final Map<String, FunkChannelDTO> channels,
          final Optional<String> authKey) {
    super(crawler, filmInfos, authKey);
    this.sessionId = new NexxCloudSessionInitiationTask(crawler).call();
    this.channels = channels;
  }

  @Override
  protected JsonDeserializer<Set<FilmUrlInfoDto>> getParser(final FilmInfoDto aDTO) {
    return new NexxCloudVideoDetailsDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<DatenFilm>>() {
    }.getType();
  }

  @Override
  protected void handleHttpError(final FilmInfoDto dto, final URI url, final Response response) {
    LOG.error(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
    if (response.getStatus() == 403) {
      final Long sessionId = new NexxCloudSessionInitiationTask(crawler).call();
      if (sessionId != null) {
        LOG.debug("403 FORBIDDEN - lets try a new session id ({} > {})", this.sessionId, sessionId);
        this.sessionId = sessionId;
      }
    }
  }

  @Override
  protected Response createResponse(final Invocation.Builder request, final FilmInfoDto dto) {
    final MultivaluedHashMap<String, String> formData = new MultivaluedStringMap();
    formData.add(POST_FORM_FIELD_ADD_STREAM_DETAILS, POST_FORM_FIELD_VALUE_ONE);
    return request
            .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
            .header(HEADER_X_REQUEST_TOKEN, REQUEST_TOKEN)
            .header(HEADER_X_REQUEST_CID, sessionId)
            .post(Entity.form(formData));
  }

  @Override
  protected void postProcessing(
          final Set<FilmUrlInfoDto> videoDetails, final FilmInfoDto filmInfo) {
    try {

      final LocalDateTime time = filmInfo.getTime();
      String dateValue = time.format(DATE_FORMAT);
      String timeValue = time.format(TIME_FORMAT);

      Map<Qualities, String> videoUrls = new EnumMap<>(Qualities.class);
      videoDetails.stream()
              .sorted(Comparator.comparingInt(FilmUrlInfoDto::getWidth))
              .forEachOrdered(details -> addIfResolutionMissing(details, videoUrls));

      if (!videoUrls.isEmpty()) {

        final String url = videoUrls.containsKey(Qualities.NORMAL) ? videoUrls.get(Qualities.NORMAL) : videoUrls.get(Qualities.SMALL);

        DatenFilm film = new DatenFilm("Funk.net", getChannelTitle(filmInfo), filmInfo.getWebsite().orElse(""), filmInfo.getTitle(),
                url, "",
                dateValue, timeValue, filmInfo.getDuration().getSeconds(), filmInfo.getDescription().orElse(""));

        if (videoUrls.containsKey(Qualities.SMALL)) {
          CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
        }
        if (videoUrls.containsKey(Qualities.HD)) {
          CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
        }
        taskResults.add(film);
      } else {
        LOG.debug(
                "The Funk film \"{}\" - \"{}\" has no download URL.", getChannelTitle(filmInfo), filmInfo.getTitle());
      }
    } catch (final Exception exception) {
      exception.printStackTrace();
    }
  }

  private String getChannelTitle(final FilmInfoDto filmInfoDto) {
    final String channelId = filmInfoDto.getTopic();
    if (channels.containsKey(channelId)) {
      return channels.get(channelId).getChannelTitle();
    }

    LOG.debug(
            "Can't find the channel {} for film info {}. Trying something different.",
            channelId,
            filmInfoDto.getTitle());
    final Optional<String> channelFromTitle = parseChannelFromTitle(filmInfoDto.getTitle());

    return channelFromTitle.orElse("");
  }

  private Optional<String> parseChannelFromTitle(final String title) {
    final List<String> titleSplits = Arrays.asList(title.split("\\|"));
    if (titleSplits.isEmpty() || titleSplits.size() < 2) {
      return Optional.empty();
    }
    // Using the last to cover things like Kickbox which includes many | and the channel is the last
    // one
    final String channelTitle = titleSplits.get(titleSplits.size() - 1).trim();
    LOG.debug("Using {} as channel title instead.", channelTitle);
    return Optional.ofNullable(channelTitle);
  }

  private void addIfResolutionMissing(final FilmUrlInfoDto details, final Map<Qualities, String> filmUrls) {
    final Qualities quality = details.getWidth() > details.getHeight() ? getQualityFromWidth(details.getWidth()) : getQualityFromWidth(details.getHeight());
    filmUrls.putIfAbsent(quality, details.getUrl());
  }

  private static Qualities getQualityFromWidth(final int width) {
    if (width >= 1280) {
      return Qualities.HD;
    }
    if (width >= 720) {
      return Qualities.NORMAL;
    }
    return Qualities.SMALL;
  }

  @Override
  protected FunkVideosToFilmsTask createNewOwnInstance(final ConcurrentLinkedQueue<FilmInfoDto> elementsToProcess) {
    return new FunkVideosToFilmsTask(
            crawler, elementsToProcess, channels, getAuthKey());
  }
}
