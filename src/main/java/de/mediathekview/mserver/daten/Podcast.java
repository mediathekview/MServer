package de.mediathekview.mserver.daten;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Podcast extends AbstractMediaResource<FilmUrl> {
  private static final long serialVersionUID = -7161315980975471103L;
  private Duration duration;
  private boolean neu;

  /** DON'T USE! - ONLY FOR GSON! */
  protected Podcast() {
    super();
    duration = null;
    neu = false;
  }

  public Podcast(
      final UUID aUuid,
      final Sender aSender,
      final String aTitel,
      final String aThema,
      final LocalDateTime aTime,
      final Duration aDauer) {
    super(aUuid, aSender, aTitel, aThema, aTime);
    duration = aDauer;
    neu = false;
  }

  public Podcast(final Podcast copyObj) {
    super(copyObj);
    duration = copyObj.duration;
    neu = copyObj.neu;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(final Duration duration) { this.duration = duration; }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Podcast)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final Podcast podcast = (Podcast) o;
    return isNeu() == podcast.isNeu() && Objects.equals(getDuration(), podcast.getDuration());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getDuration(), isNeu());
  }

  public Long getFileSizeKB(final Resolution aQuality) {
    if (getUrls().containsKey(aQuality)) {
      return getUrls().get(aQuality).getFileSize();
    } else {
      return 0L;
    }
  }

  public boolean isNeu() {
    return neu;
  }

  public void setNeu(final boolean aNeu) {
    neu = aNeu;
  }

  @Override
  public String toString() {
    return "Podcast{" +
            "duration=" + duration +
            ", neu=" + neu +
            "} " + super.toString();
  }
}
