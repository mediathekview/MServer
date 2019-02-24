package de.mediathekview.mserver.crawler.rbb.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
public class RbbTopicOverviewTaskTest extends RbbTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://mediathek.rbb-online.de/tv/Sandmann/Sendung?documentId=6503982&bcastId=6503982",
                "/rbb/rbb_topic_page_single.html",
                "",
                "",
                new String[]{
                    "https://mediathek.rbb-online.de/tv/Sandmann/Unser-Sandm%C3%A4nnchen-vom-03-03-2018/rbb-Fernsehen/Video?bcastId=6503982&documentId=50545904",
                    "https://mediathek.rbb-online.de/tv/Sandmann/Unser-Sandm%C3%A4nnchen-vom-03-03-2018-mit-Ge/rbb-Fernsehen/Video?bcastId=6503982&documentId=50546024"
                }
            },
            {
                "https://mediathek.rbb-online.de/tv/rbb-SPORT/Sendung?documentId=9597422&bcastId=9597422",
                "/rbb/rbb_topic_page_empty.html",
                "",
                "",
                new String[0]
            },
            {
                "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Sendung?documentId=7382518&bcastId=7382518",
                "/rbb/rbb_topic_page_multiple1.html",
                "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Sendung?documentId=7382518&bcastId=7382518&mcontents=page.2",
                "/rbb/rbb_topic_page_multiple2.html",
                new String[]{
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=46905564",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=46596884",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=46376984",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=46114004",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=45925538",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Auf-drei-R%C3%A4dern-von-Bayern-an-die-Ostsee/rbb-Fernsehen/Video?bcastId=7382518&documentId=45756118",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Mit-Mops-ans-Meer-2/rbb-Fernsehen/Video?bcastId=7382518&documentId=43662214",
                    "https://mediathek.rbb-online.de/tv/Kesslers-Expedition/Mit-Mops-ans-Meer-1/rbb-Fernsehen/Video?bcastId=7382518&documentId=41354390"
                }
            }
        });
  }

  private final String requestUrl1;
  private final String htmlPage1;
  private final String requestUrl2;
  private final String htmlPage2;
  private final CrawlerUrlDTO[] expectedTopics;

  public RbbTopicOverviewTaskTest(final String aRequestUrl1, final String aHtmlPage1, final String aRequestUrl2,
      final String aHtmlPage2, final String[] aExpectedUrls) {
    requestUrl1 = aRequestUrl1;
    htmlPage1 = aHtmlPage1;
    requestUrl2 = aRequestUrl2;
    htmlPage2 = aHtmlPage2;

    expectedTopics = new CrawlerUrlDTO[aExpectedUrls.length];
    for (int i = 0; i < aExpectedUrls.length; i++) {
      expectedTopics[i] = new CrawlerUrlDTO(aExpectedUrls[i]);
    }
  }

  @Test
  public void test() throws IOException {

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl1, htmlPage1);
    if (!requestUrl2.isEmpty()) {
      urlMapping.put(requestUrl2, htmlPage2);
    }
    JsoupMock.mock(urlMapping);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl1));

    final RbbTopicOverviewTask target = new RbbTopicOverviewTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expectedTopics.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedTopics));
  }
}
