package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
public class KikaTopicOverviewPageTaskTest extends KikaTaskTestBase {
  // TODO: Tests nicht lauffähig, 
  @Test
  public void testOverviewWithSinglePage() {
    // TODO: Logik für topic2_overview_page
  }
  
  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() throws IOException {
  rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(7);
    
    final String requestUrl = "https://www.kika.de/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";
    
    Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html", "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-2_zc-ad1768d3.html", "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-3_zc-c0952f36.html", "/kika/kika_topic1_overview_page1.html");
    JsoupMock.mock(mockUrls);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111178.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111180.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111214.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111252.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111250.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111248.html")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaTopicOverviewPageTask target = new KikaTopicOverviewPageTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
  
  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() throws IOException {
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumSubpages(2);
    
    final String requestUrl = "https://www.kika.de/mama-fuchs-und-papa-dachs/buendelgruppe2670.html";
    Map<String, String> mockUrls = new HashMap<>();
    mockUrls.put(requestUrl, "/kika/kika_topic1_overview_page1.html");
    mockUrls.put(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/buendelgruppe2670_page-1_zc-43c28d56.html", "/kika/kika_topic1_overview_page1.html");
    JsoupMock.mock(mockUrls);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111036.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111120.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111128.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111174.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111176.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111178.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111180.html"),
      new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + "/mama-fuchs-und-papa-dachs/sendungen/sendung111182.html")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaTopicOverviewPageTask target = new KikaTopicOverviewPageTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
