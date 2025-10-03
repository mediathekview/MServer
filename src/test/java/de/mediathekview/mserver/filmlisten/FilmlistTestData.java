package de.mediathekview.mserver.filmlisten;

import de.mediathekview.mserver.daten.*;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/** A singelton to get the test data for Filmlist tests. */
public class FilmlistTestData {
  private static FilmlistTestData instance = null;

  private FilmlistTestData() {
    super();
  }

  public static FilmlistTestData getInstance() {
    if (instance == null) {
      instance = new FilmlistTestData();
    }
    return instance;
  }

  public Collection<Film> createFilme() throws MalformedURLException {
    final Collection<Film> films = new ArrayList<>();

    final Film testFilm1 =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm1.setWebsite(URI.create("http://www.example.org/").toURL());
    testFilm1.setBeschreibung("Test beschreibung.");
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(URI.create("http://example.org/klein.mp4").toURL(), 42l));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("http://example.org/Test.mp4").toURL(), 42l));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(URI.create("http://example.org/hd.mp4").toURL(), 42l));

    final Film testFilm2 =
        new Film(
            UUID.randomUUID(),
            Sender.ARD,
            "TestTitel",
            "TestThema",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm2.setWebsite(URI.create("http://www.example.org/2").toURL());
    testFilm2.setBeschreibung("Test beschreibung.");
    testFilm2.addUrl(Resolution.SMALL, new FilmUrl(URI.create("http://example.org/klein2.mp4").toURL(), 42l));
    testFilm2.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("http://example.org/Test2.mp4").toURL(), 42l));
    testFilm2.addUrl(Resolution.HD, new FilmUrl(URI.create("http://example.org/hd2.mp4").toURL(), 42l));

    final Film testFilm3 =
        new Film(
            UUID.randomUUID(),
            Sender.BR,
            "TestTitel",
            "TestThema2",
            LocalDateTime.parse("2017-01-01T23:55:00"),
            Duration.of(10, ChronoUnit.MINUTES));
    testFilm3.setWebsite(URI.create("http://www.example.org/").toURL());
    testFilm3.setBeschreibung("Test beschreibung.");
    testFilm3.addUrl(Resolution.SMALL, new FilmUrl(URI.create("http://example.org/klein.mp4").toURL(), 42l));
    testFilm3.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("http://example.org/Test.mp4").toURL(), 42l));
    testFilm3.addUrl(Resolution.HD, new FilmUrl(URI.create("http://example.org/hd.mp4").toURL(), 42l));

    films.add(testFilm1);
    films.add(testFilm2);
    films.add(testFilm3);
    return films;
  }

  public Filmlist createTestdataNewFormat() throws MalformedURLException {
    final Filmlist testData = new Filmlist();
    testData.addAllFilms(createFilme());
    return testData;
  }
}
