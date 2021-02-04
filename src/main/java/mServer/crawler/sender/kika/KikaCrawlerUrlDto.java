package mServer.crawler.sender.kika;

import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.net.URL;
import java.util.Objects;

public class KikaCrawlerUrlDto extends CrawlerUrlDTO {

  public enum FilmType {
    NORMAL,
    AUDIO_DESCRIPTION,
    SIGN_LANGUAGE
  }

  private final FilmType filmType;

  public KikaCrawlerUrlDto(String aUrl, FilmType filmType) {
    super(aUrl);
    this.filmType = filmType;
  }

  public KikaCrawlerUrlDto(URL aUrl, FilmType filmType) {
    super(aUrl);
    this.filmType = filmType;
  }

  public FilmType getFilmType() {
    return this.filmType;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof KikaCrawlerUrlDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    KikaCrawlerUrlDto that = (KikaCrawlerUrlDto) o;
    return filmType == that.filmType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), filmType);
  }
}
