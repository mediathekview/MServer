package mServer.crawler.sender.orf.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.orf.OrfConstants;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OrfHistoryOverviewTask implements Callable<ConcurrentLinkedQueue<TopicUrlDTO>> {

  private static final String ATTRIBUTE_HREF = "href";
  private static final String ATTRIBUTE_TITLE = "title";
  private static final String TOPIC_URL_SELECTOR = "section.has-4-in-row article > a";

  private final MediathekReader crawler;
  private final JsoupConnection jsoupConnection;

  public OrfHistoryOverviewTask(
          final MediathekReader aCrawler) {
    crawler = aCrawler;
    jsoupConnection = new JsoupConnection();
  }

  @Override
  public ConcurrentLinkedQueue<TopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document = jsoupConnection.getDocument(OrfConstants.URL_ARCHIVE);

    final Elements topics = document.select(TOPIC_URL_SELECTOR);
    topics.forEach(
            topicElement -> {
              final String url = topicElement.attr(ATTRIBUTE_HREF);
              final String topic = topicElement.attr(ATTRIBUTE_TITLE);
              results.add(new TopicUrlDTO(topic, url));
            });

    return results;
  }
}
