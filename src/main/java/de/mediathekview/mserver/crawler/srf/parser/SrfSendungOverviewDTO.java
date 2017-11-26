package de.mediathekview.mserver.crawler.srf.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SrfSendungOverviewDTO {
  
  private final List<String> urls;
  private Optional<String> nextPageId;

  public SrfSendungOverviewDTO() {
    urls = new ArrayList<>();
    nextPageId = Optional.empty();
  }

  public boolean addUrl(final String aUrl) {
    return urls.add(aUrl);
  }
  
  public Optional<String> getNextPageId() {
    return nextPageId;
  }

  public List<String> getUrls() {
    return urls;
  }  
  
  public void setNextPageId(final Optional<String> aNextPageId) {
    nextPageId = aNextPageId;
  }
}
