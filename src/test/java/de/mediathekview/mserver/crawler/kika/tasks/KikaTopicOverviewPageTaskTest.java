package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KikaTopicOverviewPageTaskTest extends KikaTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testOverviewWithSinglePageWithBoxBroadcastLayout() throws IOException {
    final String requestUrl =
        getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/buendelgruppe2624.html";

    final Connection connection =
        JsoupMock.mock(requestUrl, "/kika/kika_topic2_overview_page.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108102.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108104.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108136.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108138.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108140.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108142.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108144.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/alles-neu-fuer-lina/sendungen/sendung108146.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewWithSinglePageWithoutBoxBroadcastLayout() throws IOException {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(1);

    final String requestUrl = getWireMockBaseUrlSafe() + "/pur/sendungen/videos-pur-102.html";

    final Connection connection =
        JsoupMock.mock(requestUrl, "/kika/kika_topic6_overview_no_boxbroadcast.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/pur/sendungen/sendung133864.html", FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/pur/sendungen/sendung133128.html", FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/pur/sendungen/sendung132534.html", FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe()
                  + "/pur/sendungen/blobbing-horsing-und-co-spass-oder-sport-102.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(7);

    final String requestUrl =
        getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";

    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html",
        "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    final Map<String, Connection> connections = JsoupMock.mock(mockUrls);
    connections.forEach(
        (url, currentConnection) -> {
          try {
            when(jsoupConnection.getConnection(eq(url))).thenReturn(currentConnection);
          } catch (final IOException iox) {
            fail();
          }
        });

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(2);

    final String requestUrl =
        getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    final Map<String, Connection> connections = JsoupMock.mock(mockUrls);
    connections.forEach(
        (url, currentConnection) -> {
          try {
            when(jsoupConnection.getConnection(eq(url))).thenReturn(currentConnection);
          } catch (final IOException iox) {
            fail();
          }
        });

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewLandingPageLinksNotToFirstPageSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(3);

    final String requestUrl =
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-0_zc-6615e895.html",
        "/kika/kika_topic1_overview_page1.html");
    final Map<String, Connection> connections = JsoupMock.mock(mockUrls);
    connections.forEach(
        (url, currentConnection) -> {
          try {
            when(jsoupConnection.getConnection(url)).thenReturn(currentConnection);
          } catch (final IOException iox) {
            fail();
          }
        });

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewLandingPageLinksNotToFirstPageWithSubpagesLargerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(5);

    final String requestUrl =
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-0_zc-6615e895.html",
        "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        getWireMockBaseUrlSafe()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    final Map<String, Connection> connections = JsoupMock.mock(mockUrls);
    connections.forEach(
        (url, currentConnection) -> {
          try {
            when(jsoupConnection.getConnection(eq(url))).thenReturn(currentConnection);
          } catch (final IOException iox) {
            fail();
          }
        });

    final KikaCrawlerUrlDto[] expected =
        new KikaCrawlerUrlDto[] {
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html",
              FilmType.NORMAL),
          new KikaCrawlerUrlDto(
              getWireMockBaseUrlSafe() + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html",
              FilmType.NORMAL)
        };

    actAndAssert(requestUrl, expected);
  }

  private void actAndAssert(final String requestUrl, final KikaCrawlerUrlDto[] expected) {
    final Queue<KikaCrawlerUrlDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new KikaCrawlerUrlDto(requestUrl, FilmType.NORMAL));

    final KikaTopicOverviewPageTask target =
        new KikaTopicOverviewPageTask(
            createCrawler(), urls, getWireMockBaseUrlSafe(), jsoupConnection);
    final Set<KikaCrawlerUrlDto> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
