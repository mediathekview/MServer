package de.mediathekview.mserver.crawler.arte;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ArteSendungOverviewDto {
  private final List<ArteFilmUrlDto> urls;
  private Optional<String> nextPageId;

  public ArteSendungOverviewDto() {
    urls = new ArrayList<>();
    nextPageId = Optional.empty();
  }

  public boolean addUrl(final ArteFilmUrlDto aUrl) {
    return urls.add(aUrl);
  }

  public Optional<String> getNextPageId() {
    return nextPageId;
  }

  public List<ArteFilmUrlDto> getUrls() {
    return urls;
  }

  public void setNextPageId(final String aNextPageId) {
    nextPageId = Optional.of(aNextPageId);
  }

  public void setNextPageId(final Optional<String> aNextPageId) {
    nextPageId = aNextPageId;
  }
}
