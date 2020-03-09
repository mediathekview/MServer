package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Objects;

public class ZdfFilmDto extends CrawlerUrlDTO {

  private final Film film;

  public ZdfFilmDto(final Film film, final String videoUrl) {
    super(videoUrl);
    this.film = film;
  }

  public Film getFilm() {
    return film;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZdfFilmDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ZdfFilmDto that = (ZdfFilmDto) o;

    return Objects.equals(film, that.film);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (film != null ? film.hashCode() : 0);
    return result;
  }

}
