package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.daten.Film;

import java.util.HashSet;
import java.util.Set;

public class ArdFilmDto {

  private final Film film;
  private final Set<ArdFilmInfoDto> relatedFilms;

  public ArdFilmDto(final Film film) {
    this.film = film;
    relatedFilms = new HashSet<>();
  }

  public Film getFilm() {
    return film;
  }

  public Set<ArdFilmInfoDto> getRelatedFilms() {
    return relatedFilms;
  }

  public void addRelatedFilm(final ArdFilmInfoDto filmInfoDto) {
    relatedFilms.add(filmInfoDto);
  }
}
