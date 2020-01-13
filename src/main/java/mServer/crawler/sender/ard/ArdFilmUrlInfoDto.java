package mServer.crawler.sender.ard;

import mServer.crawler.sender.base.FilmUrlInfoDto;

public class ArdFilmUrlInfoDto extends FilmUrlInfoDto {

  private final String quality;

  public ArdFilmUrlInfoDto(final String aUrl, final String aQuality) {
    super(aUrl);
    quality = aQuality;
  }

  public String getQuality() {
    return quality;
  }
}
