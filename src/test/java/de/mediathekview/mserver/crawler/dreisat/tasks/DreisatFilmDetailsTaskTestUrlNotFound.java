package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DreisatFilmDetailsTaskTestUrlNotFound extends DreisatTaskTestBase {

  @Test
  public void testXmlUrlNotExisting() {
    final String requestUrl = "/mediathek/?mode=play&obj=411651151";
    final String xmlRequestUrl = "/mediathek/xmlservice.php/v3/web/beitragsDetails?id=411651151";

    setupResponseWithoutBody(xmlRequestUrl, 404);
    final Set<Film> actual = executeTask(requestUrl);

    assertThat(actual.size(), equalTo(0));
  }

  private Set<Film> executeTask(final String requestUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + requestUrl));
      return new DreisatFilmDetailsTask(
              createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE, WireMockTestBase.MOCK_URL_BASE)
              .invoke();
  }
}
