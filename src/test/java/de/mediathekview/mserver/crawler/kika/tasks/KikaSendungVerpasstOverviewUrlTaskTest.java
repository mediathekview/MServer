package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Set;
import org.junit.Test;

public class KikaSendungVerpasstOverviewUrlTaskTest extends KikaTaskTestBase {

  @Test
  public void callTestNoFutureUrls() {

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(0);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[] {
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html")

    };

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler());

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestWithFutureUrls() {

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(4);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(3);

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[] {
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-07032019_zc-992c124d.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-08032019_zc-b34a6c22.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-b2e97756.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-11032019_zc-0c865c8b.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-12032019_zc-08dd781a.html"),
        new CrawlerUrlDTO("https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-13032019_zc-d37093d7.html")
    };

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler());

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void callTestRangeLargerThanAvailableDays() {

    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSection(40);
    rootConfig.getSenderConfig(Sender.KIKA).setMaximumDaysForSendungVerpasstSectionFuture(30);

    KikaSendungVerpasstOverviewUrlTask target = new KikaSendungVerpasstOverviewUrlTask(createCrawler());

    Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(28));
  }
}