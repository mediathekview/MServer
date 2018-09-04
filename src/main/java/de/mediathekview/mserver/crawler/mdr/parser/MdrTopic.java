package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MdrTopic {

  private Set<CrawlerUrlDTO> filmUrls;
  private Optional<CrawlerUrlDTO> nextPage;

  public MdrTopic() {
    filmUrls = new HashSet<>();
    nextPage = Optional.empty();
  }

  public void addFilmUrl(final CrawlerUrlDTO aUrlDto) {
    filmUrls.add(aUrlDto);
  }

  public Set<CrawlerUrlDTO> getFilmUrls() {
    return filmUrls;
  }

  public Optional<CrawlerUrlDTO> getNextPage() {
    return nextPage;
  }

  public void setNextPage(final CrawlerUrlDTO aUrlDto) {
    nextPage = Optional.of(aUrlDto);
  }
}
