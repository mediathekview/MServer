package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mlib.daten.Film;
import java.util.HashSet;
import java.util.Set;

public class ArdFilmDto {

  private Film film;
  private Set<ArdFilmInfoDto> relatedFilms;

  public ArdFilmDto(final Film film) {
    this.film = film;
    this.relatedFilms = new HashSet<>();
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
