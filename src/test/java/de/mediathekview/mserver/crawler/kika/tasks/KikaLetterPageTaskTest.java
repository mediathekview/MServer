package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
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
public class KikaLetterPageTaskTest extends KikaTaskTestBase {
  
  public KikaLetterPageTaskTest() {
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/sendungen/sendungenabisz100_page-V_zc-1fc26dc3.html";
    JsoupMock.mock(requestUrl, "/kika/kika_letter_pageV.html");

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO("https://www.kika.de/verbotene-geschichten/sendereihe290.html"),
        new CrawlerUrlDTO("https://www.kika.de/verknallt-abgedreht/sendereihe2128.html"),
        new CrawlerUrlDTO("https://www.kika.de/vier-kartoffeln/sendereihe2124.html")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaLetterPageTask target = new KikaLetterPageTask(createCrawler(), urls, KikaConstants.BASE_URL);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
  
}
