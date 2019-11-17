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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

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
      final Long aSessionId,
      final ConcurrentLinkedQueue<FilmInfoDto> aFilmInfos,
      final Map<String, FunkChannelDTO> aChannels,
      final Optional<String> authKey) {
    super(crawler, aFilmInfos, authKey);
    sessionId = aSessionId;
    channels = aChannels;
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
        String.format(
            "A HTTP error %d occurred when getting REST information from: \"%s\".",
            response.getStatus(), url.toString()));
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
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.FUNK,
            filmInfo.getTitle(),
            channels.get(filmInfo.getTopic()).getChannelTitle(),
            filmInfo.getTime(),
            filmInfo.getDuration());
    film.addGeolocation(GeoLocations.GEO_NONE);
    film.setBeschreibung(filmInfo.getDescription());
    addWebsite(filmInfo, film);
    videoDetails.stream()
        .sorted(Comparator.comparingInt(FilmUrlInfoDto::getWidth))
        .forEachOrdered(details -> addIfResolutionMissing(details, film));

    if (film.getUrls().isEmpty()) {
      LOG.debug(
          String.format(
              "The Funk film \"%s\" - \"%s\" has no download URL.",
              film.getThema(), film.getTitel()));
      crawler.incrementAndGetErrorCount();
    } else {
      taskResults.add(film);
      crawler.incrementAndGetActualCount();
    }
    crawler.updateProgress();
  }

  private void addIfResolutionMissing(final FilmUrlInfoDto details, final Film film) {
    final Resolution resolution = Resolution.getResolutionFromWidth(details.getWidth());
    try {
      film.addUrlIfAbsent(resolution, new FilmUrl(details.getUrl(), sessionId));
    } catch (final MalformedURLException malformedURLException) {
      LOG.error("Invalid Funk Video Url: " + details.getUrl(), malformedURLException);
    }
  }

  private void addWebsite(final FilmInfoDto filmInfo, final Film film) {
    try {
      film.setWebsite(new URL(filmInfo.getWebsite()));
    } catch (final MalformedURLException malformedURLException) {
      LOG.error(
          String.format("The website url \"%s\" isn't valid!", filmInfo.getWebsite()),
          malformedURLException);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected FunkVideosToFilmsTask createNewOwnInstance(
      final ConcurrentLinkedQueue<FilmInfoDto> aElementsToProcess) {
    return new FunkVideosToFilmsTask(crawler, sessionId, aElementsToProcess, channels, authKey);
  }
}
