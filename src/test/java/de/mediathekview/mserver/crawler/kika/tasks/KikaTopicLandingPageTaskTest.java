package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore({
  "com.sun.org.apache.xerces.*",
  "javax.xml.*",
  "org.xml.*",
  "org.w3c.*",
  "com.sun.org.apache.xalan.*",
  "javax.net.ssl.*"
})
public class KikaTopicLandingPageTaskTest extends KikaTaskTestBase {
  public KikaTopicLandingPageTaskTest(
      final String aRequestUrl, final String aHtmlFile, final CrawlerUrlDTO[] aExpectedUrls) {
    requestUrl = aRequestUrl;
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  private final String requestUrl;
  private final String htmlFile;
  private final CrawlerUrlDTO[] expectedUrls;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://www.kika.de/mama-fuchs-und-papa-dachs/sendereihe2694.html",
            "/kika/kika_topic1_landing_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.kika.de/mama-fuchs-und-papa-dachs/buendelgruppe2670.html")
            }
          },
          {
            "https://www.kika.de/singalarm/sendungen/sendung105928.html",
            "/kika/kika_topic2_landing_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO(
                  "https://www.kika.de/singalarm/sendungen/buendelgruppe2234_page-2_zc-d5c4767c_zs-e540764b.html")
            }
          },
          {
            "https://www.kika.de/alles-neu-fuer-lina/sendereihe2648.html",
            "/kika/kika_topic3_landing_page.html",
            new CrawlerUrlDTO[] {
              new CrawlerUrlDTO("https://www.kika.de/alles-neu-fuer-lina/buendelgruppe2624.html")
            }
          }
        });
  }

  @Test
  public void testLandingPageWithMoreButton() throws IOException {
    JsoupMock.mock(requestUrl, htmlFile);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaTopicLandingPageTask target =
        new KikaTopicLandingPageTask(createCrawler(), urls, KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, containsInAnyOrder(expectedUrls));
  }
}
