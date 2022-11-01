package de.mediathekview.mserver.crawler.kika.json;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.funk.FunkChannelDTO;

public class KikaApiBrandsDto {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<CrawlerUrlDTO> nextUrl = Optional.empty();
  private Set<TopicUrlDTO> elements = new HashSet<>();
  
  public void add(TopicUrlDTO aTopicUrlDTO) {
    elements.add(aTopicUrlDTO);
  }
  
  public Set<TopicUrlDTO> getElements() {
    return elements;
  }
  
  public Optional<CrawlerUrlDTO> getNextPage() {
    return nextUrl;
  }
  
  public void setNextPage(CrawlerUrlDTO aCrawlerUrlDTO) {
    nextUrl = Optional.of(aCrawlerUrlDTO);
  }

  public void setError(Optional<String> aErrorCode, Optional<String> aErrorMesssage) {
    errorCode = aErrorCode;
    errorMesssage = aErrorMesssage;
  }
  
  public Optional<String> getErrorMesssage() {
    return errorMesssage;
  }

  public Optional<String> getErrorCode() {
    return errorCode;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final KikaApiBrandsDto o = (KikaApiBrandsDto) obj;
    return errorMesssage.equals(o.errorMesssage) &&
        errorCode.equals(o.errorCode) &&
        nextUrl.equals(o.nextUrl) &&
        elements.equals(o.elements);
  }
}
