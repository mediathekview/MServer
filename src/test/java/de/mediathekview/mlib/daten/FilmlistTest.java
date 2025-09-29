package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FilmlistTest {

  @Test
  void testMerge() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final Film testFilm2 = createTestFilm2();
    final Podcast testPodcast1 = createTestPodcast1();
    final Podcast testPodcast2 = createTestPodcast2();
    final Livestream testLivestream1 = createTestLivestream1();
    final Livestream testLivestream2 = createTestLivestream2();

    final Filmlist filmlist1 = new Filmlist();
    filmlist1.add(testFilm1);
    filmlist1.add(testFilm2);
    filmlist1.add(testPodcast2);
    filmlist1.add(testLivestream1);

    final Filmlist filmlist2 = new Filmlist();
    filmlist2.add(testFilm2);
    filmlist2.add(testFilm1);
    filmlist2.add(testPodcast1);
    filmlist2.add(testLivestream1);
    filmlist2.add(testLivestream2);

    final Filmlist differenceList = filmlist1.merge(filmlist2);
    assertThat(differenceList.getFilms()).isEmpty();
    assertThat(differenceList.getPodcasts()).hasSize(1).allSatisfy((currentKey, currentElement) -> assertThat(currentElement).isEqualTo(testPodcast1));
    assertThat(differenceList.getLivestreams()).hasSize(1).allSatisfy((currentKey, currentElement) -> assertThat(currentElement).isEqualTo(testLivestream2));
  }

  private Film createTestFilm1() throws MalformedURLException {
    final Film testFilm1 = new Film(UUID.randomUUID(), Sender.ARD, "TestTitel", "TestThema",
        LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES));
    testFilm1.setWebsite(new URL("http://www.example.org/"));
    testFilm1.setBeschreibung("Test beschreibung.");
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein.mp4"), 42L));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test.mp4"), 42L));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd.mp4"), 42L));
    return testFilm1;
  }

  private Film createTestFilm2() throws MalformedURLException {
    final Film testFilm2 = new Film(UUID.randomUUID(), Sender.ARD, "TestTitel", "TestThema",
        LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES));
    testFilm2.setWebsite(new URL("http://www.example.org/2"));
    testFilm2.setBeschreibung("Test beschreibung.");
    testFilm2.addUrl(Resolution.SMALL, new FilmUrl(new URL("http://example.org/klein2.mp4"), 42L));
    testFilm2.addUrl(Resolution.NORMAL, new FilmUrl(new URL("http://example.org/Test2.mp4"), 42L));
    testFilm2.addUrl(Resolution.HD, new FilmUrl(new URL("http://example.org/hd2.mp4"), 42L));
    return testFilm2;
  }

  private Livestream createTestLivestream1() throws MalformedURLException {
    final Livestream testLivestream1 = new Livestream(UUID.randomUUID(), Sender.ZDF, "Livestream 1",
        "Livestream", LocalDateTime.now());
    testLivestream1.setWebsite(new URL("https://zdf.de"));
    testLivestream1.addUrl(Resolution.HD, new URL("http://example.org/hd.mp4"));
    return testLivestream1;
  }

  private Livestream createTestLivestream2() throws MalformedURLException {
    final Livestream testLivestream2 = new Livestream(UUID.randomUUID(), Sender.ZDF, "Livestream 2",
        "Livestream", LocalDateTime.now());
    testLivestream2.setWebsite(new URL("https://zdf.de"));
    testLivestream2.addUrl(Resolution.NORMAL, new URL("http://example.org/normal.mp4"));
    return testLivestream2;
  }

  private Podcast createTestPodcast1() throws MalformedURLException {
    final Podcast testPodcast1 = new Podcast(UUID.randomUUID(), Sender.BR, "Podcast 1", "Thema 1",
        LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES));
    testPodcast1.setWebsite(new URL("http://www.example.org/2"));
    testPodcast1.addUrl(Resolution.NORMAL,
        new FilmUrl(new URL("http://example.org/normal.mp3"), 42l));
    return testPodcast1;
  }

  private Podcast createTestPodcast2() throws MalformedURLException {
    final Podcast testPodcast2 = new Podcast(UUID.randomUUID(), Sender.BR, "Podcast 2", "Thema 1",
        LocalDateTime.parse("2017-01-01T23:55:00"), Duration.of(10, ChronoUnit.MINUTES));
    testPodcast2.setWebsite(new URL("http://www.example.org/2"));
    testPodcast2.addUrl(Resolution.NORMAL,
        new FilmUrl(new URL("http://example.org/normal.mp3"), 42l));
    return testPodcast2;
  }

}
