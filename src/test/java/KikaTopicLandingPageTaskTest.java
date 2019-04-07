import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.tasks.KikaTaskTestBase;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class KikaTopicLandingPageTaskTest extends KikaTaskTestBase {
  
  public KikaTopicLandingPageTaskTest() {
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/mama-fuchs-und-papa-dachs/sendereihe2694.html";
    JsoupMock.mock(requestUrl, "/kika/kika_topic1_landing_page.html");

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO("https://www.kika.de/mama-fuchs-und-papa-dachs/buendelgruppe2670.html")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaTopicLandingPageTask target = new KikaTopicLandingPageTask(createCrawler(), urls, KikaConstants.BASE_URL);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
  
}
