package mServer.crawler.sender.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A simple Data-Transfer-Object to get the elements and the optionally found
 * next page link.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 * <b>Mail:</b> nicklas@wiegandt.eu<br>
 * <b>Jabber:</b> nicklas2751@elaon.de<br>
 * <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class PagedElementListDTO<T> {

  private final Set<T> elements;
  private Optional<String> nextPage;

  public PagedElementListDTO() {
    super();
    elements = new HashSet<>();
    nextPage = Optional.empty();
  }

  public boolean addElements(final Collection<T> elements) {
    return this.elements.addAll(elements);
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
    if (!(obj instanceof PagedElementListDTO)) {
      return false;
    }
    final PagedElementListDTO other = (PagedElementListDTO) obj;
    if (elements == null) {
      if (other.elements != null) {
        return false;
      }
    } else if (!elements.equals(other.elements)) {
      return false;
    }
    if (nextPage == null) {
      return other.nextPage == null;
    } else {
      return nextPage.equals(other.nextPage);
    }
  }

  public Set<T> getElements() {
    return elements;
  }

  public Optional<String> getNextPage() {
    return nextPage;
  }

  public void setNextPage(final Optional<String> aNextPage) {
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
    result = prime * result + (elements == null ? 0 : elements.hashCode());
    result = prime * result + (nextPage == null ? 0 : nextPage.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "PagedElementListDTO{" + "elements=" + elements + ", nextPage=" + nextPage + '}';
  }

  public boolean addElement(final T element) {
    return elements.add(element);
  }
}
