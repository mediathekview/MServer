package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class KikaSendungVerpasstOverviewUrlTaskTest extends KikaTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  Document htmlPage;

  KikaCrawler crawler;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    jsoupConnection = JsoupMock.mock(KikaConstants.URL_DAY_PAGE, "/kika/kika_days_overview.html");
    crawler = createCrawler();
    crawler.setConnection(jsoupConnection);
  }

  private final LocalDateTime today = LocalDateTime.of(2019, 3, 10, 0, 0, 0);

  @Test
  public void callTestNoFutureUrls() throws IOException {
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(0);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html")
        };

    final KikaSendungVerpasstOverviewUrlTask target =
        new KikaSendungVerpasstOverviewUrlTask(crawler, today);

    final Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestWithFutureUrls() throws IOException {
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(3);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-11032019_zc-0c865c8b.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-12032019_zc-08dd781a.html"),
          new CrawlerUrlDTO(
              "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-13032019_zc-d37093d7.html")
        };

    final KikaSendungVerpasstOverviewUrlTask target =
        new KikaSendungVerpasstOverviewUrlTask(crawler, today);

    final Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestRangeLargerThanAvailableDays() throws IOException {
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(40);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(30);

    final KikaSendungVerpasstOverviewUrlTask target =
        new KikaSendungVerpasstOverviewUrlTask(crawler, today);

    final Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(28));
  }
}
