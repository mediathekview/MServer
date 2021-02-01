package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Connection;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KikaLetterPageUrlTaskTest extends KikaTaskTestBase {

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Mock JsoupConnection jsoupConnection;

  public KikaLetterPageUrlTaskTest() {}

  @Test
  public void test() throws IOException {
    final String requestUrl = KikaConstants.URL_TOPICS_PAGE;
    final Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_letter_pageA.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              "https://www.kika.de/sendungen/sendungenabisz100_page-A_zc-05fb1331.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/sendungen/sendungenabisz100_page-Q_zc-2cb019d6.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/sendungen/sendungenabisz100_page-V_zc-1fc26dc3.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/sendungen/sendungenabisz100_page-Y_zc-388beba7.html",
              FilmType.NORMAL)
        };

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.NORMAL));

    final KikaLetterPageUrlTask target =
        new KikaLetterPageUrlTask(createCrawler(), urls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void testDgs() throws IOException {
    final String requestUrl = KikaConstants.URL_DGS_PAGE;
    final Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_gbs1.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/videos-dgs-100_page-0_zc-6615e895.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/videos-dgs-100_page-1_zc-43c28d56.html",
              FilmType.SIGN_LANGUAGE),
          new KikaCrawlerUrlDto(
              "https://www.kika.de/videos/alle-dgs/videos-dgs-100_page-2_zc-ad1768d3.html",
              FilmType.SIGN_LANGUAGE)
        };

    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.SIGN_LANGUAGE));

    final KikaLetterPageUrlTask target =
        new KikaLetterPageUrlTask(createCrawler(), urls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
