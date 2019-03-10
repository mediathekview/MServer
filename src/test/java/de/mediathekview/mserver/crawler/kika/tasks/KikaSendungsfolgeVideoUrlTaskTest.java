package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.*;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
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
public class KikaSendungsfolgeVideoUrlTaskTest extends KikaTaskTestBase {

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/rocket-ich/sendungen/sendung41184.html";
    JsoupMock.mock(requestUrl, "/kika/kika_film1.html");

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[] {
      new CrawlerUrlDTO("https://www.kika.de/rocket-ich/sendungen/videos/video14406-avCustom.xml")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaSendungsfolgeVideoUrlTask target = new KikaSendungsfolgeVideoUrlTask(createCrawler(), urls);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}