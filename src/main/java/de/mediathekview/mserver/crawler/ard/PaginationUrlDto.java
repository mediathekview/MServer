package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.HashSet;
import java.util.Set;

public class PaginationUrlDto {
  private final Set<CrawlerUrlDTO> urls = new HashSet<>();
  private int actualPage;
  private int maxPages;

  public void addUrl(CrawlerUrlDTO url) {
    urls.add(url);
  }

  public void addAll(Set<CrawlerUrlDTO> urls) {
    this.urls.addAll(urls);
  }

  public Set<CrawlerUrlDTO> getUrls() {
    return urls;
  }

  public int getActualPage() {
    return actualPage;
  }

  public int getMaxPages() {
    return maxPages;
  }

  public void setActualPage(int actualPage) {
    this.actualPage = actualPage;
  }

  public void setMaxPages(int maxPages) {
    this.maxPages = maxPages;
  }
}
