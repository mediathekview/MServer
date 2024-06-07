package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.Objects;
import java.util.Optional;

public class ZdfFilmDto {

  private final Optional<Film> film;
  private final Optional<String> urlSignLanguage;
  private final Optional<String> videoUrl;

  public ZdfFilmDto(final Optional<Film> film, final String videoUrl, String urlSignLanguage) {
    this.film = film;
    if (videoUrl == null) {
      this.videoUrl = Optional.empty();
    } else {
      this.videoUrl = Optional.of(videoUrl);
    }

    if (urlSignLanguage != null && !urlSignLanguage.isEmpty()) {
      this.urlSignLanguage = Optional.of(urlSignLanguage);
    } else {
      this.urlSignLanguage = Optional.empty();
    }
  }

  public Optional<String> getUrl() {
    return videoUrl;
  }
  
  public Optional<Film> getFilm() {
    return film;
  }

  public Optional<String> getUrlSignLanguage() {
    return urlSignLanguage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ZdfFilmDto that = (ZdfFilmDto) o;
    return Objects.equals(film, that.film) && Objects.equals(urlSignLanguage, that.urlSignLanguage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), film, urlSignLanguage);
  }
}
