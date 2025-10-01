package de.mediathekview.mlib.daten;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FilmEqualsTest {
  Film createTestFilm1() throws MalformedURLException {
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
    testFilm1.addUrl(Resolution.SMALL, new FilmUrl(URI.create("http://example.org/klein.mp4").toURL(), 42L));
    testFilm1.addUrl(Resolution.NORMAL, new FilmUrl(URI.create("http://example.org/Test.mp4").toURL(), 42L));
    testFilm1.addUrl(Resolution.HD, new FilmUrl(URI.create("http://example.org/hd.mp4").toURL(), 42L));
    return testFilm1;
  }

  @Test
  void testEqualsBeschreibung() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung("testEqualsBeschreibung");
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsCopy() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsGeoLocations() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();
    final List<GeoLocations> geoLocs = new ArrayList<>();
    geoLocs.add(GeoLocations.GEO_DE);
    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setGeoLocations(geoLocs);
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsTime() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            LocalDateTime.now(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsUrls() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsUUID() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            UUID.randomUUID(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testEqualsWebsite() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(URI.create("http://localhost/").toURL());
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isEqualTo(testFilm2);
    assertThat(testFilm1).hasSameHashCodeAs(testFilm2);
  }

  @Test
  void testNotEqualsDuration() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            Duration.of(42, ChronoUnit.MINUTES));
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isNotEqualTo(testFilm2);
    assertThat(testFilm1.hashCode()).isNotEqualTo(testFilm2.hashCode());
  }

  @Test
  void testNotEqualsSender() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            Sender.ARTE_FR,
            testFilm1.getTitel(),
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isNotEqualTo(testFilm2);
    assertThat(testFilm1.hashCode()).isNotEqualTo(testFilm2.hashCode());
  }

  @Test
  void testNotEqualsThema() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            testFilm1.getTitel(),
            "testNotEqualsThema",
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isNotEqualTo(testFilm2);
    assertThat(testFilm1.hashCode()).isNotEqualTo(testFilm2.hashCode());
  }

  @Test
  void testNotEqualsTitel() throws MalformedURLException {
    final Film testFilm1 = createTestFilm1();

    final Film testFilm2 =
        new Film(
            testFilm1.getUuid(),
            testFilm1.getSender(),
            "testNotEqualsTitel",
            testFilm1.getThema(),
            testFilm1.getTime(),
            testFilm1.getDuration());
    testFilm2.setWebsite(testFilm1.getWebsite().orElse(null));
    testFilm2.setBeschreibung(testFilm1.getBeschreibung());
    testFilm1.getUrls().forEach(testFilm2::addUrl);

    assertThat(testFilm1).isNotEqualTo(testFilm2);
    assertThat(testFilm1.hashCode()).isNotEqualTo(testFilm2.hashCode());
  }
}
