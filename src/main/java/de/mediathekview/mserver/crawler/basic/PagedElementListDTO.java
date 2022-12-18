package de.mediathekview.mserver.crawler.basic;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/** A simple Data-Transfer-Object to get the elements and the optionally found next page link. */
public class PagedElementListDTO<T> {
  private final Set<T> elements = new HashSet<>();
  private Optional<String> nextPage = Optional.empty();

  public PagedElementListDTO() {
    super();
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
    if (!elements.equals(other.elements)) {
      return false;
    }
    return nextPage.equals(other.nextPage);
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
    result = prime * result + (elements.isEmpty() ? 0 : elements.hashCode());
    result = prime * result + (nextPage.isEmpty() ? 0 : nextPage.hashCode());
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
