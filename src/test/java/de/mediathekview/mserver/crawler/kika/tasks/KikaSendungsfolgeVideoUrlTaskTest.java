package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;

public class KikaSendungsfolgeVideoUrlTaskTest extends KikaTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/rocket-ich/sendungen/sendung41184.html";
    jsoupConnection = JsoupMock.mock(requestUrl, "/kika/kika_film1.html");
    KikaCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              "https://www.kika.de/rocket-ich/sendungen/videos/video14406-avCustom.xml", FilmType.NORMAL)
        };

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.NORMAL));

    final KikaSendungsfolgeVideoUrlTask target =
        new KikaSendungsfolgeVideoUrlTask(crawler, urls);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
