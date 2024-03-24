package mServer.crawler.sender.kika;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import mServer.crawler.sender.base.TopicUrlDTO;


public class KikaApiTopicDto {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<TopicUrlDTO> nextUrl = Optional.empty();
  private Set<KikaApiFilmDto> elements = new HashSet<>();
  
  public void add(KikaApiFilmDto aKikaApiFilmDto) {
    elements.add(aKikaApiFilmDto);
  }
  
  public Set<KikaApiFilmDto> getElements() {
    return elements;
  }
  
  public Optional<TopicUrlDTO> getNextPage() {
    return nextUrl;
  }
  
  public void setNextPage(TopicUrlDTO aCrawlerUrlDTO) {
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
  
}
