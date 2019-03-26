package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class OrfHistoryOverviewTask implements Callable<ConcurrentLinkedQueue<TopicUrlDTO>> {

  private static final String TOPIC_URL_SELECTOR = "section.has-4-in-row article > a";

  private final AbstractCrawler crawler;

  public OrfHistoryOverviewTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ConcurrentLinkedQueue<TopicUrlDTO> call() throws Exception {
    final ConcurrentLinkedQueue<TopicUrlDTO> results = new ConcurrentLinkedQueue<>();

    // URLs für Seiten parsen
    final Document document =
        Jsoup.connect(OrfConstants.URL_ARCHIVE)
            .timeout(
                (int)
                    TimeUnit.SECONDS.toMillis(
                        crawler.getCrawlerConfig().getSocketTimeoutInSeconds()))
            .get();

    final Elements topics = document.select(TOPIC_URL_SELECTOR);
    topics.forEach(
        topicElement -> {
          final String url = topicElement.attr(Consts.ATTRIBUTE_HREF);
          final String topic = topicElement.attr(Consts.ATTRIBUTE_TITLE);
          results.add(new TopicUrlDTO(topic, url));
        });

    return results;
  }
}
