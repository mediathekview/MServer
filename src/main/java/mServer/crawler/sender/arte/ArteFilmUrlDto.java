package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.util.Objects;

public class ArteFilmUrlDto extends CrawlerUrlDTO {

  private final String videoDetailsUrl;
  private String sender = Const.ARTE_DE;

  public ArteFilmUrlDto(String aFilmDetailsUrl, String aVideoDetailsUrl) {
    super(aFilmDetailsUrl);
    this.videoDetailsUrl = aVideoDetailsUrl;
  }

  public String getSender() {
    return sender;
  }

  public CrawlerUrlDTO getVideoDetailsUrl() {
    return new CrawlerUrlDTO(videoDetailsUrl);
  }

  public void setSender(String sender) { this.sender = sender; }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArteFilmUrlDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ArteFilmUrlDto that = (ArteFilmUrlDto) o;
    return Objects.equals(videoDetailsUrl, that.videoDetailsUrl) && Objects.equals(sender, that.sender);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), sender, videoDetailsUrl);
  }
}
