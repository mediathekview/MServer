package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.parser.WdrFilmDeserializer;
import de.mediathekview.mserver.crawler.wdr.parser.WdrFilmPartDeserializer;
import org.jsoup.nodes.Document;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WdrFilmDetailTask extends AbstractDocumentTask<Film, TopicUrlDTO> {

  public WdrFilmDetailTask(
      final AbstractCrawler aCrawler,
      final Queue<TopicUrlDTO> aUrlToCrawlDtos,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
  }

  @Override
  protected void processDocument(final TopicUrlDTO aUrlDto, final Document aDocument) {
    final WdrFilmDeserializer deserializer =
        new WdrFilmDeserializer(getProtocol(aUrlDto), crawler.getSender());
    final WdrFilmPartDeserializer partDeserializer = new WdrFilmPartDeserializer();
    processParts(partDeserializer.deserialize(aUrlDto.getTopic(), aDocument));

    final Optional<Film> film = deserializer.deserialize(aUrlDto, aDocument);
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
  protected AbstractUrlTask<Film, TopicUrlDTO> createNewOwnInstance(
      final Queue<TopicUrlDTO> aUrlsToCrawl) {
    return new WdrFilmDetailTask(crawler, aUrlsToCrawl, getJsoupConnection());
  }

  private String getProtocol(final TopicUrlDTO aUrlDto) {
    String protocol = "https:";

    final Optional<String> usedProtocol = UrlUtils.getProtocol(aUrlDto.getUrl());
    if (usedProtocol.isPresent()) {
      protocol = usedProtocol.get();
    }

    return protocol;
  }

  private void processParts(final Set<TopicUrlDTO> aParts) {
    if (aParts.isEmpty()) {
      return;
    }

    final Queue<TopicUrlDTO> queue = new ConcurrentLinkedQueue<>(aParts);
    taskResults.addAll(createNewOwnInstance(queue).invoke());
  }
}
