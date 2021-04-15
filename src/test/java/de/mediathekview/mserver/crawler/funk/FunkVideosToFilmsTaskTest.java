package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.funk.tasks.FunkVideosToFilmsTask;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class FunkVideosToFilmsTaskTest extends FunkTaskTestBase {

  private static final String TITLE =
      "Ansage an Hater, Reue, Reichtum uvm. | Money Boy im Talk + Live Performance";
  private static final String THEMA = "World Wide Wohnzimmer";
  private static final @NotNull LocalDateTime TIME = parseTime();
  private static final Duration DAUER = Duration.ofSeconds(855);
  private static final String BESCHREIBUNG =
      "Skrrt! Skrrt! Der Boy steppt ins Wohnzimmer und talkt über Hater, Reue, Reichtum und ein mögliches Feature mit Helene Fischer... Viel Fun! Yo!";
  private static final String WEBSITE =
      "https://www.funk.net/channel/world-wide-wohnzimmer-1045/ansage-an-hater-reue-reichtum-uvm-money-boy-im-talk-live-performance-1605930";

  @NotNull
  private static LocalDateTime parseTime() {
    return LocalDateTime.parse(
        "2019-04-21T15:00:00.000+0000", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
  }

  @Test
  public void testVideoToFilm() throws MalformedURLException {
    setupSuccessfulJsonPostResponse(
        "/v3/741/videos/byid/1605930", "/funk/nexx_cloud_video_details.json");

    final Queue<FilmInfoDto> filmInfos = new ConcurrentLinkedQueue<>();
    final FilmInfoDto filmInfo =
        new FilmInfoDto(getWireMockBaseUrlSafe() + "/v3/741/videos/byid/1605930");
    filmInfo.setTopic("1045");
    filmInfo.setTitle(TITLE);
    filmInfo.setTime(TIME);
    filmInfo.setDuration(DAUER);
    filmInfo.setDescription(BESCHREIBUNG);
    filmInfo.setWebsite(WEBSITE);
    filmInfos.offer(filmInfo);

    final Map<String, FunkChannelDTO> channels = new HashMap<>();
    channels.put("1045", new FunkChannelDTO("1045", THEMA));

    final Film actual = executeTask(filmInfos, channels).iterator().next();
    assertThat(actual, equalTo(createCorrectFilm(actual.getUuid())));
  }

  private Film createCorrectFilm(final UUID uuid) throws MalformedURLException {
    final Film film = new Film(uuid, Sender.FUNK, TITLE, THEMA, TIME, DAUER);
    film.setBeschreibung(BESCHREIBUNG);
    film.setWebsite(new URL(WEBSITE));
    addCorrectDownloadUrls(film);
    return film;
  }

  private void addCorrectDownloadUrls(final Film film) throws MalformedURLException {
    film.addUrl(
        Resolution.UHD,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_3840x2160_16000.mp4"),
            0L));
    film.addUrl(
        Resolution.WQHD,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_2560x1440_9000.mp4"),
            0L));
    film.addUrl(
        Resolution.HD,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1920x1080_6000.mp4"),
            0L));
    film.addUrl(
        Resolution.NORMAL,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1280x720_2500.mp4"),
            0L));
    film.addUrl(
        Resolution.SMALL,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_640x360_700.mp4"),
            0L));
    film.addUrl(
        Resolution.VERY_SMALL,
        new FilmUrl(
            new URL(
                "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_320x180_400.mp4"),
            0L));
  }

  private Set<Film> executeTask(
      final Queue<FilmInfoDto> filmInfos, final Map<String, FunkChannelDTO> channels) {
    final FunkCrawler crawler = createCrawler();
    return new FunkVideosToFilmsTask(crawler, filmInfos, channels, null).invoke();
  }
}
