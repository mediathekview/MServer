package de.mediathekview.mserver.crawler.funk.json;

import de.mediathekview.mserver.crawler.arte.ArteJsonElementDto;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A simple Data-Transfer-Object to get the found channels and the optionally found next page link.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Jabber:</b> nicklas2751@elaon.de<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class FunkChannelListDTO {
  private final Set<FunkChannelDTO> foundChannel;
  private Optional<URI> nextPage;

  public FunkChannelListDTO() {
    super();
    foundChannel = new HashSet<>();
    nextPage = Optional.empty();
  }

  public boolean addFoundFilm(final ArteJsonElementDto element) {
    return foundChannel.add(element);
  }

  public boolean addFoundFilms(final Collection<ArteJsonElementDto> elements) {
    return foundChannel.addAll(elements);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof FunkChannelListDTO)) {
      return false;
    }
    final FunkChannelListDTO other = (FunkChannelListDTO) obj;
    if (foundChannel == null) {
      if (other.foundChannel != null) {
        return false;
      }
    } else if (!foundChannel.equals(other.foundChannel)) {
      return false;
    }
    if (nextPage == null) {
      return other.nextPage == null;
    } else {
      return nextPage.equals(other.nextPage);
    }
  }

  public Set<ArteJsonElementDto> getFoundChannel() {
    return foundChannel;
  }

  public Optional<URI> getNextPage() {
    return nextPage;
  }

  public void setNextPage(final Optional<URI> aNextPage) {
    nextPage = aNextPage;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (foundChannel == null ? 0 : foundChannel.hashCode());
    result = prime * result + (nextPage == null ? 0 : nextPage.hashCode());
    return result;
  }
}
