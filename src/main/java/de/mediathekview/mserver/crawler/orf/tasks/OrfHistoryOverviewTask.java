package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

public class OrfHistoryOverviewTask implements Callable<Queue<TopicUrlDTO>> {

  private static final String TOPIC_URL_SELECTOR = "section.has-4-in-row article > a";

  private final AbstractCrawler crawler;

  JsoupConnection jsoupConnection;

  public OrfHistoryOverviewTask(
      final AbstractCrawler aCrawler, final JsoupConnection jsoupConnection) {
    crawler = aCrawler;
    this.jsoupConnection = jsoupConnection;
  }

  @Override
  public Queue<TopicUrlDTO> call() throws Exception {
    final Queue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document =
        jsoupConnection.getDocumentTimeoutAfter(
            OrfConstants.URL_ARCHIVE,
            (int)
                TimeUnit.SECONDS.toMillis(crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));

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
