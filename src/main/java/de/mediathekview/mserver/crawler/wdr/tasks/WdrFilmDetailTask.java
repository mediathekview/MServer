package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.parser.WdrFilmDeserializer;
import de.mediathekview.mserver.crawler.wdr.parser.WdrFilmPartDeserializer;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;

public class WdrFilmDetailTask extends AbstractDocumentTask<Film, TopicUrlDTO> {

  public WdrFilmDetailTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<TopicUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  @Override
  protected void processDocument(TopicUrlDTO aUrlDto, Document aDocument) {
    WdrFilmDeserializer deserializer = new WdrFilmDeserializer(getProtocol(aUrlDto), crawler.getSender());
    WdrFilmPartDeserializer partDeserializer = new WdrFilmPartDeserializer();
    processParts(partDeserializer.deserialize(aUrlDto.getTopic(), aDocument));

    Optional<Film> film = deserializer.deserialize(aUrlDto, aDocument);
    if (film.isPresent()) {
      taskResults.add(film.get());
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    } else {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractUrlTask<Film, TopicUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<TopicUrlDTO> aUrlsToCrawl) {
    return new WdrFilmDetailTask(crawler, aUrlsToCrawl);
  }

  private String getProtocol(TopicUrlDTO aUrlDto) {
    String protocol = "https:";

    Optional<String> usedProtocol = UrlUtils.getProtocol(aUrlDto.getUrl());
    if (usedProtocol.isPresent()) {
      protocol = usedProtocol.get();
    }

    return protocol;
  }

  private void processParts(final Set<TopicUrlDTO> aParts) {
    if (aParts.isEmpty()) {
      return;
    }

    final ConcurrentLinkedQueue<TopicUrlDTO> queue = new ConcurrentLinkedQueue<>(aParts);
    taskResults.addAll(createNewOwnInstance(queue).invoke());
  }
}
