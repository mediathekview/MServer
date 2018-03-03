package de.mediathekview.mserver.crawler.rbb.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class RbbTopicsOverviewTaskTest extends RbbTaskTestBase {

  @Test
  public void test() throws IOException {

    JsoupMock.mock(RbbConstants.URL_TOPICS_A_K, "/rbb/rbb_topics1.html");

    final TopicUrlDTO[] expected = new TopicUrlDTO[]{
        new TopicUrlDTO("30 Favoriten", "http://mediathek.rbb-online.de/tv/30-Favoriten/Sendung?documentId=17399976&bcastId=17399976"),
        new TopicUrlDTO("Abendschau", "http://mediathek.rbb-online.de/tv/Abendschau/Sendung?documentId=3822076&bcastId=3822076"),
        new TopicUrlDTO("Abendshow", "http://mediathek.rbb-online.de/tv/Abendshow/Sendung?documentId=45722438&bcastId=45722438"),
        new TopicUrlDTO("Abenteuer...", "http://mediathek.rbb-online.de/tv/Abenteuer-/Sendung?documentId=41967306&bcastId=41967306"),
        new TopicUrlDTO("Karneval der Kulturen",
            "http://mediathek.rbb-online.de/tv/Karneval-der-Kulturen/Sendung?documentId=43155502&bcastId=43155502"),
        new TopicUrlDTO("Kesslers Expedition",
            "http://mediathek.rbb-online.de/tv/Kesslers-Expedition/Sendung?documentId=7382518&bcastId=7382518"),
        new TopicUrlDTO("Kowalski & Schmidt",
            "http://mediathek.rbb-online.de/tv/Kowalski-Schmidt/Sendung?documentId=16361776&bcastId=16361776")
    };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_A_K));

    final RbbTopicsOverviewTask target = new RbbTopicsOverviewTask(createCrawler(), urls);
    final Set<TopicUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
  }
}
