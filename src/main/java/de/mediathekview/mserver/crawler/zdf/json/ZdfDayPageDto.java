package de.mediathekview.mserver.crawler.zdf.json;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class ZdfDayPageDto {

  private final Collection<TopicUrlDTO> entries;
  private Optional<String> nextPageUrl;

  public ZdfDayPageDto() {
    entries = new ArrayList<>();
    nextPageUrl = Optional.empty();
  }

  public void addEntry(TopicUrlDTO entry) {
    entries.add(entry);
  }

  public Collection<TopicUrlDTO> getEntries() {
    return entries;
  }

  public Optional<String> getNextPageUrl() {
    return nextPageUrl;
  }

  public void setNextPageUrl(final String aUrl) {
    nextPageUrl = Optional.of(aUrl);
  }
}
