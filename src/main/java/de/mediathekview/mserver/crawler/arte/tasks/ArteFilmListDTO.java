package de.mediathekview.mserver.crawler.arte.tasks;

import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import de.mediathekview.mserver.crawler.arte.ArteJsonElementDto;

/**
 * A simple Data-Transfer-Object to get the found films and the optionally found next page link.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class ArteFilmListDTO {
  private final Set<ArteJsonElementDto> foundFilms;
  private Optional<URI> nextPage;

  public ArteFilmListDTO() {
    super();
    foundFilms = new HashSet<>();
    nextPage = Optional.empty();
  }

  public boolean addFoundFilm(final ArteJsonElementDto element) {
    return foundFilms.add(element);
  }

  public boolean addFoundFilms(final Collection<ArteJsonElementDto> elements) {
    return foundFilms.addAll(elements);
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
    if (!(obj instanceof ArteFilmListDTO)) {
      return false;
    }
    final ArteFilmListDTO other = (ArteFilmListDTO) obj;
    if (foundFilms == null) {
      if (other.foundFilms != null) {
        return false;
      }
    } else if (!foundFilms.equals(other.foundFilms)) {
      return false;
    }
    if (nextPage == null) {
      if (other.nextPage != null) {
        return false;
      }
    } else if (!nextPage.equals(other.nextPage)) {
      return false;
    }
    return true;
  }

  public Set<ArteJsonElementDto> getFoundFilms() {
    return foundFilms;
  }

  public Optional<URI> getNextPage() {
    return nextPage;
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
    result = prime * result + (foundFilms == null ? 0 : foundFilms.hashCode());
    result = prime * result + (nextPage == null ? 0 : nextPage.hashCode());
    return result;
  }

  public void setNextPage(final Optional<URI> aNextPage) {
    nextPage = aNextPage;
  }

}
