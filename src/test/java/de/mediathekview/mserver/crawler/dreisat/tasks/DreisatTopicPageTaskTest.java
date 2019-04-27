package de.mediathekview.mserver.crawler.dreisat.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;

public class DreisatTopicPageTaskTest extends DreisatTaskTestBase {

  @Test
  public void testNoSubpages() {
    final String requestUrl = "/mediathek/?red=film";
    setupSuccessfulResponse(requestUrl, "/dreisat/dreisat_topic_page1.html");
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl, 1);

    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testWithOneSubpage() {
    final String requestUrl = "/mediathek/?red=film";
    setupSuccessfulResponse(requestUrl, "/dreisat/dreisat_topic_page1.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst1", "/dreisat/dreisat_topic_page2.html");
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl, 2);

    assertThat(actual.size(), equalTo(20));
  }

  @Test
  public void testWithAllSubpages() {
    final String requestUrl = "/mediathek/?red=film";
    setupSuccessfulResponse(requestUrl, "/dreisat/dreisat_topic_page1.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst1", "/dreisat/dreisat_topic_page2.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst2", "/dreisat/dreisat_topic_page3.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst3", "/dreisat/dreisat_topic_page4.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst4", "/dreisat/dreisat_topic_page5.html");
    setupSuccessfulResponse(requestUrl + "&mode=verpasst5", "/dreisat/dreisat_topic_page6.html");
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl, 6);

    assertThat(actual.size(), equalTo(58));
  }

  private Set<CrawlerUrlDTO> executeTask(String aRequestUrl, int aSubPages) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + aRequestUrl));
    return new DreisatTopicPageTask(
        createCrawler(), urls, false, aSubPages)
        .invoke();
  }
}