package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore({
  "com.sun.org.apache.xerces.*",
  "javax.xml.*",
  "org.xml.*",
  "org.w3c.*",
  "com.sun.org.apache.xalan.*",
  "javax.net.ssl.*"
})
public class KikaTopicOverviewPageTaskTest extends KikaTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() throws IOException {
    final String requestUrl =
        wireMockServer.baseUrl() + "/alles-neu-fuer-lina/buendelgruppe2624.html";

    JsoupMock.mock(requestUrl, "/kika/kika_topic2_overview_page.html");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108102.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108104.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108136.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108138.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108140.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108142.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108144.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl() + "/alles-neu-fuer-lina/sendungen/sendung108146.html")
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(7);

    final String requestUrl =
        wireMockServer.baseUrl() + "/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";

    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html",
        "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    JsoupMock.mock(mockUrls);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html")
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(2);

    final String requestUrl =
        wireMockServer.baseUrl() + "/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    JsoupMock.mock(mockUrls);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html")
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewLandingPageLinksNotToFirstPageSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(3);

    final String requestUrl =
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-0_zc-6615e895.html",
        "/kika/kika_topic1_overview_page1.html");
    JsoupMock.mock(mockUrls);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html")
        };

    actAndAssert(requestUrl, expected);
  }

  @Test
  public void testOverviewLandingPageLinksNotToFirstPageWithSubpagesLargerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(5);

    final String requestUrl =
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html";
    final Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page3.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html",
        "/kika/kika_topic1_overview_page4.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-0_zc-6615e895.html",
        "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(
        wireMockServer.baseUrl()
            + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html",
        "/kika/kika_topic1_overview_page2.html");
    JsoupMock.mock(mockUrls);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html"),
          new CrawlerUrlDTO(
              wireMockServer.baseUrl()
                  + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html")
        };

    actAndAssert(requestUrl, expected);
  }

  private void actAndAssert(final String requestUrl, final CrawlerUrlDTO[] expected) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaTopicOverviewPageTask target =
        new KikaTopicOverviewPageTask(createCrawler(), urls, wireMockServer.baseUrl());
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
