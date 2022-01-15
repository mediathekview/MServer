package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.*;
import de.mediathekview.mserver.base.config.CrawlerApiParam;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.basic.FilmUrlInfoDto;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudVideoDetailsDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;

import javax.annotation.Nullable;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

public class FunkVideosToFilmsTask
    extends AbstractJsonRestTask<Film, Set<FilmUrlInfoDto>, FilmInfoDto> {
  private static final Logger LOG = LogManager.getLogger(FunkVideosToFilmsTask.class);
  private static final String POST_FORM_FIELD_ADD_STREAM_DETAILS = "addStreamDetails";
  private static final String POST_FORM_FIELD_VALUE_ONE = "1";
  private static final String HEADER_X_REQUEST_CID = "x-request-cid";
  private static final String HEADER_X_REQUEST_TOKEN = "x-request-token";
  private final String requestToken;
  private Long sessionId;
  private final transient Map<String, FunkChannelDTO> channels;

  public FunkVideosToFilmsTask(
      final AbstractCrawler crawler,
      final Queue<FilmInfoDto> filmInfos,
      final Map<String, FunkChannelDTO> channels,
      @Nullable final String authKey) {
    super(crawler, filmInfos, authKey);
    this.sessionId = new NexxCloudSessionInitiationTask(crawler).call();
    this.requestToken = crawler.getRuntimeConfig().getCrawlerApiParam(CrawlerApiParam.FUNK_REQUEST_TOKEN).orElse("");
    this.channels = channels;
  }

  @Override
  protected JsonDeserializer<Set<FilmUrlInfoDto>> getParser(final FilmInfoDto aDTO) {
    return new NexxCloudVideoDetailsDeserializer(crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<Film>>() {}.getType();
  }

  @Override
  protected void handleHttpError(final FilmInfoDto dto, final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.error(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    crawler.incrementAndGetErrorCount();
    if (response.getStatus() == 403) {
      final Long newSessionId = new NexxCloudSessionInitiationTask(crawler).call();
      if (newSessionId != null) {
        LOG.debug("403 FORBIDDEN - lets try a new session id ({} > {})", this.sessionId, newSessionId);
        this.sessionId = newSessionId;
      }
    }
  }

  @Override
  protected Response createResponse(final Invocation.Builder request, final FilmInfoDto dto) {
    final MultivaluedHashMap<String, String> formData = new MultivaluedStringMap();
    formData.add(POST_FORM_FIELD_ADD_STREAM_DETAILS, POST_FORM_FIELD_VALUE_ONE);
    return request
        .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
        .header(HEADER_X_REQUEST_TOKEN, requestToken)
        .header(HEADER_X_REQUEST_CID, sessionId)
        .post(Entity.form(formData));
  }

  @Override
  protected void postProcessing(
      final Set<FilmUrlInfoDto> videoDetails, final FilmInfoDto filmInfo) {
    try {
      final Film film =
          new Film(
              UUID.randomUUID(),
              Sender.FUNK,
              filmInfo.getTitle(),
              getChannelTitle(filmInfo),
              filmInfo.getTime(),
              filmInfo.getDuration());
      film.addGeolocation(GeoLocations.GEO_NONE);
      filmInfo.getDescription().ifPresent(film::setBeschreibung);
      filmInfo.getWebsite().ifPresent(website -> addWebsite(website, film));
      videoDetails.stream()
          .sorted(Comparator.comparingInt(FilmUrlInfoDto::getWidth))
          .forEachOrdered(details -> addIfResolutionMissing(details, film));

      if (film.getUrls().isEmpty()) {
        LOG.debug(
            "The Funk film \"{}\" - \"{}\" has no download URL.", film.getThema(), film.getTitel());
        crawler.incrementAndGetErrorCount();
      } else {
        if (!taskResults.add(film)) {
          taskResults.forEach( aFilmEntry -> {
            if (aFilmEntry.equals(film)) {
              LOG.debug("Duplicate entry {} vs {} on url {}", film, aFilmEntry, filmInfo.getUrl());
            }
          });
        }
        crawler.incrementAndGetActualCount();
      }
      crawler.updateProgress();
    } catch (final Exception exception) {
      LOG.error(exception);
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
    final String channelTitle = titleSplits.get(titleSplits.size() - 1).strip();
    LOG.debug("Using {} as channel title instead.", channelTitle);
    return Optional.ofNullable(channelTitle);
  }

  private void addIfResolutionMissing(final FilmUrlInfoDto details, final Film film) {
    // some videos are vertical video => use height instead of width
    final int width = Math.max(details.getWidth(), details.getHeight());
    final Resolution resolution = Resolution.getResolutionFromWidth(width);
    try {
      film.addUrlIfAbsent(resolution, new FilmUrl(details.getUrl(), 0L));
    } catch (final MalformedURLException malformedURLException) {
      LOG.error("Invalid Funk Video Url: {}", details.getUrl(), malformedURLException);
    }
  }

  private void addWebsite(final String website, final Film film) {
    try {
      film.setWebsite(new URL(website));
    } catch (final MalformedURLException malformedURLException) {
      LOG.error(
          String.format("The website url \"%s\" isn't valid!", website), malformedURLException);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected FunkVideosToFilmsTask createNewOwnInstance(final Queue<FilmInfoDto> elementsToProcess) {
    return new FunkVideosToFilmsTask(
        crawler, elementsToProcess, channels, getAuthKey().orElse(null));
  }
}
