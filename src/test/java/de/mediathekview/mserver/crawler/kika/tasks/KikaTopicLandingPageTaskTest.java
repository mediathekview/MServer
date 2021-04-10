package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(Parameterized.class)
public class KikaTopicLandingPageTaskTest extends KikaTaskTestBase {

  public KikaTopicLandingPageTaskTest(
      final String aRequestUrl, final String aHtmlFile, final KikaCrawlerUrlDto[] aExpectedUrls) {
    requestUrl = aRequestUrl;
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  private final String requestUrl;
  private final String htmlFile;
  private final KikaCrawlerUrlDto[] expectedUrls;

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://www.kika.de/mama-fuchs-und-papa-dachs/sendereihe2694.html",
            "/kika/kika_topic1_landing_page.html",
            new KikaCrawlerUrlDto[] {
              new KikaCrawlerUrlDto(
                  "https://www.kika.de/mama-fuchs-und-papa-dachs/buendelgruppe2670.html", FilmType.NORMAL)
            }
          },
          {
            "https://www.kika.de/singalarm/sendungen/sendung105928.html",
            "/kika/kika_topic2_landing_page.html",
            new KikaCrawlerUrlDto[] {
              new KikaCrawlerUrlDto(
                  "https://www.kika.de/singalarm/sendungen/buendelgruppe2234_page-2_zc-d5c4767c_zs-e540764b.html", FilmType.NORMAL),
                new KikaCrawlerUrlDto("https://www.kika.de/singalarm/sendungen/videos-singalarm-100.html", FilmType.NORMAL)
            }
          },
          {
            "https://www.kika.de/alles-neu-fuer-lina/sendereihe2648.html",
            "/kika/kika_topic3_landing_page.html",
            new KikaCrawlerUrlDto[] {
              new KikaCrawlerUrlDto("https://www.kika.de/alles-neu-fuer-lina/buendelgruppe2624.html", FilmType.NORMAL)
            }
          },
          {
            "https://www.kika.de/sendungen/special/s/schnitzeljagd/uebersicht-116.html",
            "/kika/kika_topic4_is_overview_page_without_all_button.html",
            new KikaCrawlerUrlDto[] {
              new KikaCrawlerUrlDto(
                  "https://www.kika.de/sendungen/special/s/schnitzeljagd/uebersicht-116.html", FilmType.NORMAL)
            }
          },
          {
            "https://www.kika.de/tib-tumtum/tib-und-tumtum-180.html",
            "/kika/kika_topic5_only_new_videos_link.html",
            new KikaCrawlerUrlDto[] {
              new KikaCrawlerUrlDto("https://www.kika.de/tib-tumtum/buendelgruppe2730.html", FilmType.NORMAL)
            }
          }
        });
  }

  @Test
  public void testLandingPageWithMoreButton() throws IOException {
    jsoupConnection = JsoupMock.mock(requestUrl, htmlFile);
    KikaCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.NORMAL));

    final KikaTopicLandingPageTask target =
        new KikaTopicLandingPageTask(
            crawler, urls, KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, containsInAnyOrder(expectedUrls));
  }
}
