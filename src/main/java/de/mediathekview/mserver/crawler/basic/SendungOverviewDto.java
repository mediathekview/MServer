package de.mediathekview.mserver.crawler.basic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SendungOverviewDto {

  private final List<CrawlerUrlDTO> urls;
  private Optional<String> nextPageId;

  public SendungOverviewDto() {
    urls = new ArrayList<>();
    nextPageId = Optional.empty();
  }

  public boolean addUrl(final String aUrl) {
    return urls.add(new CrawlerUrlDTO(aUrl));
  }

  public Optional<String> getNextPageId() {
    return nextPageId;
  }

  public List<CrawlerUrlDTO> getUrls() {
    return urls;
  }

  public void setNextPageId(final String aNextPageId) {
    nextPageId = Optional.of(aNextPageId);
  }

  public void setNextPageId(final Optional<String> aNextPageId) {
    nextPageId = aNextPageId;
  }
}
