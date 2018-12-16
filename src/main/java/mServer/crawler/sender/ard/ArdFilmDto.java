package mServer.crawler.sender.ard;

import de.mediathekview.mlib.daten.DatenFilm;
import java.util.HashSet;
import java.util.Set;

public class ArdFilmDto {

  private final DatenFilm film;
  private final Set<ArdFilmInfoDto> relatedFilms;

  public ArdFilmDto(final DatenFilm film) {
    this.film = film;
    this.relatedFilms = new HashSet<>();
  }

  public DatenFilm getFilm() {
    return film;
  }

  public Set<ArdFilmInfoDto> getRelatedFilms() {
    return relatedFilms;
  }

  public void addRelatedFilm(final ArdFilmInfoDto filmInfoDto) {
    relatedFilms.add(filmInfoDto);
  }
}
