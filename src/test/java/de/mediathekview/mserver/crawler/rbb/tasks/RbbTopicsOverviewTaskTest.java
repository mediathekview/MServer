package de.mediathekview.mserver.crawler.rbb.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
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

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
    };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_A_K));

    final RbbTopicsOverviewTask target = new RbbTopicsOverviewTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
