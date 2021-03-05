package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OrfHistoryOverviewTask implements Callable<Queue<TopicUrlDTO>> {

  private static final String TOPIC_URL_SELECTOR = "section.has-4-in-row article > a";

  private final AbstractCrawler crawler;

  public OrfHistoryOverviewTask(
      final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Queue<TopicUrlDTO> call() throws Exception {
    final Queue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document = crawler.getConnection().getDocument(OrfConstants.URL_ARCHIVE);

    final Elements topics = document.select(TOPIC_URL_SELECTOR);
    topics.forEach(
        topicElement -> {
          final String url = topicElement.attr(HtmlConsts.ATTRIBUTE_HREF);
          final String topic = topicElement.attr(HtmlConsts.ATTRIBUTE_TITLE);
          results.add(new TopicUrlDTO(topic, url));
        });

    return results;
  }
}
