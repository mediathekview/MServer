package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
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
import static org.hamcrest.Matchers.containsInAnyOrder;

public class KikaLetterPageTaskTest extends KikaTaskTestBase {

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Mock JsoupConnection jsoupConnection;

  public KikaLetterPageTaskTest() {}

  @Test
  public void test() throws IOException {
    final String requestUrl =
        "https://www.kika.de/sendungen/sendungenabisz100_page-V_zc-1fc26dc3.html";
    jsoupConnection = JsoupMock.mock(requestUrl, "/kika/kika_letter_pageV.html");
    KikaCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              "https://www.kika.de/verbotene-geschichten/sendereihe290.html", FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/verknallt-abgedreht/sendereihe2128.html", FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/vier-kartoffeln/sendereihe2124.html", FilmType.NORMAL)
        };

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.NORMAL));

    final KikaLetterPageTask target =
        new KikaLetterPageTask(crawler, urls, KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void testSignLanguage() throws IOException {
    final String requestUrl = getWireMockBaseUrlSafe() + "/videos/alle-gbs/videos-gbs-100.html";

    jsoupConnection = JsoupMock.mock(requestUrl, "/kika/kika_gbs1.html");
    KikaCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video80444_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/neun-karl-den-grossen-104_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/zehn-napoleon-102_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video79956_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video80104_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video80452_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video80450_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video80448_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video79540_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/video79286_zc-32cf7dfb_zs-c6524396.html",
              FilmType.SIGN_LANGUAGE)
        };

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.SIGN_LANGUAGE));
    final KikaLetterPageTask target =
        new KikaLetterPageTask(crawler, urls, KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
