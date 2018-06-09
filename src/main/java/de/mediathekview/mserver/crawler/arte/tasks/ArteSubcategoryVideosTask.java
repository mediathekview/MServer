package de.mediathekview.mserver.crawler.arte.tasks;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mserver.crawler.arte.ArteCrawlerUrlDto;
import de.mediathekview.mserver.crawler.arte.json.ArteSubcategoryVideosDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteSubcategoryVideosTask extends ArteSendungVerpasstTask {
  private static final long serialVersionUID = 4115389346933900914L;

  public ArteSubcategoryVideosTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<ArteCrawlerUrlDto> aUrlToCrawlDTOs,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected Object getParser(final ArteCrawlerUrlDto aDTO) {
    return new ArteSubcategoryVideosDeserializer(crawler, aDTO.getCategory());
  }

}
