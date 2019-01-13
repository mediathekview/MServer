package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArteSubcategoryUrlDto {
  private final List<TopicUrlDTO> urls;
  private Optional<String> nextPageId;

  public ArteSubcategoryUrlDto() {
    urls = new ArrayList<>();
    nextPageId = Optional.empty();
  }

  public boolean addUrl(final TopicUrlDTO aUrl) {
    return urls.add(aUrl);
  }

  public Optional<String> getNextPageId() {
    return nextPageId;
  }

  public List<TopicUrlDTO> getUrls() {
    return urls;
  }

  public void setNextPageId(final String aNextPageId) {
    nextPageId = Optional.of(aNextPageId);
  }

  public void setNextPageId(final Optional<String> aNextPageId) {
    nextPageId = aNextPageId;
  }
}
