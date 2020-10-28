package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.*;
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
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
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
  private static final String REQUEST_TOKEN = "f058a27469d8b709c3b9db648cae47c2";
  private final Long sessionId;
  private final Map<String, FunkChannelDTO> channels;

  public FunkVideosToFilmsTask(
      final AbstractCrawler crawler,
      final Long sessionId,
      final Queue<FilmInfoDto> filmInfos,
      final Map<String, FunkChannelDTO> channels,
      @Nullable final String authKey) {
    super(crawler, filmInfos, authKey);
    this.sessionId = sessionId;
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
  protected void handleHttpError(final URI url, final Response response) {
    crawler.printErrorMessage();
    LOG.error(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    crawler.incrementAndGetErrorCount();
  }

  @Override
  protected Response createResponse(final Invocation.Builder request) {
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
        taskResults.add(film);
        crawler.incrementAndGetActualCount();
      }
      crawler.updateProgress();
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
        filmInfoDto);
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
    final Resolution resolution = Resolution.getResolutionFromWidth(details.getWidth());
    try {
      film.addUrlIfAbsent(resolution, new FilmUrl(details.getUrl(), sessionId));
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
        crawler, sessionId, elementsToProcess, channels, getAuthKey().orElse(null));
  }
}
