package de.mediathekview.mserver.crawler.basic;

import org.junit.Test;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class IgnoreFilmFilterTest {

  @Test
  public void filterTest() {
    final IgnoreFilmFilter ignoreFilmFilter = new IgnoreFilmFilter("ignoreFilmlist.txt");
    //
    assertThat(ignoreFilmFilter.size(), equalTo(4));
    //
    final Film filmPositiv0 = new Film(UUID.randomUUID(), Sender.FUNK, "title A", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmPositiv1 = new Film(UUID.randomUUID(), Sender.FUNK, "title A (Audiodescription)", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmPositiv2 = new Film(UUID.randomUUID(), Sender.FUNK, "TiTlE a", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmNegativ = new Film(UUID.randomUUID(), Sender.FUNK, "title B", "", LocalDateTime.now(), Duration.ofSeconds(120));
    final Film filmNegativ1 = new Film(UUID.randomUUID(), Sender.FUNK, "title", "", LocalDateTime.now(), Duration.ofSeconds(120));
    //
    assertThat(ignoreFilmFilter.ignoreFilm(filmPositiv0), equalTo(true));
    assertThat(ignoreFilmFilter.ignoreFilm(filmPositiv1), equalTo(true));
    assertThat(ignoreFilmFilter.ignoreFilm(filmPositiv2), equalTo(true));
    assertThat(ignoreFilmFilter.ignoreFilm(filmNegativ), equalTo(false));
    assertThat(ignoreFilmFilter.ignoreFilm(filmNegativ1), equalTo(false));
  }

}
