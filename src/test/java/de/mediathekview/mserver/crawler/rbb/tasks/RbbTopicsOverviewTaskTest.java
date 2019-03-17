package de.mediathekview.mserver.crawler.rbb.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
        value = {
                "javax.net.ssl.*",
                "javax.*",
                "com.sun.*",
                "org.apache.logging.log4j.core.config.xml.*"
        })
public class RbbTopicsOverviewTaskTest extends RbbTaskTestBase {

  @Test
  public void test() throws IOException {

    JsoupMock.mock(RbbConstants.URL_TOPICS_A_K, "/rbb/rbb_topics1.html");

      final CrawlerUrlDTO[] expected =
              new CrawlerUrlDTO[]{
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/30-Favoriten/Sendung?documentId=17399976&bcastId=17399976"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Abendschau/Sendung?documentId=3822076&bcastId=3822076"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Abendshow/Sendung?documentId=45722438&bcastId=45722438"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Abenteuer-/Sendung?documentId=41967306&bcastId=41967306"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Karneval-der-Kulturen/Sendung?documentId=43155502&bcastId=43155502"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Sendung?documentId=7382518&bcastId=7382518"),
                      new CrawlerUrlDTO(
                              "https://mediathek.rbb-online.de/tv/Kowalski-Schmidt/Sendung?documentId=16361776&bcastId=16361776")
              };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_A_K));

    final RbbTopicsOverviewTask target = new RbbTopicsOverviewTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
