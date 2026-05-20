package de.mediathekview.mserver.crawler.tagesschau;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.HashSet;
import java.util.Set;

public class EntryUrlDto {
  private final Set<CrawlerUrlDTO> videos;
  private final Set<CrawlerUrlDTO> subPages;

  public EntryUrlDto() {
    this.videos = new HashSet<>();
    this.subPages = new HashSet<>();
  }

  public Set<CrawlerUrlDTO> getVideos() {
    return videos;
  }

  public Set<CrawlerUrlDTO> getSubPages() {
    return subPages;
  }

  public void addVideo(CrawlerUrlDTO videoUrl) {
    this.videos.add(videoUrl);
  }

  public void addSubPage(CrawlerUrlDTO subPageUrl) {
    this.subPages.add(subPageUrl);
  }
}