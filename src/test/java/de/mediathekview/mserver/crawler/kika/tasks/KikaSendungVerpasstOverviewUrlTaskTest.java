package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class KikaSendungVerpasstOverviewUrlTaskTest extends KikaTaskTestBase {

  private final LocalDateTime today = LocalDateTime.of(2019, 03, 10, 0, 0, 0);

  @Test
  public void callTestNoFutureUrls() throws IOException {

    JsoupMock.mock(KikaConstants.URL_DAY_PAGE, "/kika/kika_days_overview.html");

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(0);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html")

    };

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler(), today);

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestWithFutureUrls() throws IOException {
    JsoupMock.mock(KikaConstants.URL_DAY_PAGE, "/kika/kika_days_overview.html");

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(3);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-11032019_zc-0c865c8b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-12032019_zc-08dd781a.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-13032019_zc-d37093d7.html")
    };

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler(), today);

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestRangeLargerThanAvailableDays() throws IOException {
    JsoupMock.mock(KikaConstants.URL_DAY_PAGE, "/kika/kika_days_overview.html");

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(40);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(30);

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler(), today);

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(28));
  }
}