package de.mediathekview.mserver.crawler.srf.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungOverviewDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class SrfSendungOverviewPageTaskTest {
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589);

  @Before
  public void setUp() {
    
  }
  
  @Test
  public void testOverviewWithSinglePage() {
    String[] expectedUrls = new String[] { 
      "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/69cf918f-185a-4806-92f6-031e7f09844d.json",
      "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/4eb1dbdf-dab8-4690-ba93-fdbafebbd5de.json",
      "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/af4c9505-c265-49f6-86c8-67fe90dd0a2f.json",
      "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json"
    };

    String requestUrl = "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";
    String jsonBody = FileReader.readFile("/srf/srf_sendung_overview_page_last.json");
    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));

    final Set<SrfSendungOverviewDTO> actual = executeTask(requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
    
    SrfSendungOverviewDTO actualDto = actual.toArray(new SrfSendungOverviewDTO[0])[0];
    assertThat(actualDto, notNullValue());
    assertThat(actualDto.getNextPageId(), equalTo(Optional.empty()));
    assertThat(actualDto.getUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }  

  @Test
  public void testOverviewPageNotFound() {
    String requestUrl = "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";
    
    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
      .willReturn(aResponse()
        .withStatus(404)
        .withBody("Not Found")));
    
    final Set<SrfSendungOverviewDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }  
  
  private static Set<SrfSendungOverviewDTO> executeTask(String requestUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO("http://localhost:8589" + requestUrl));
    
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
    
    SrfCrawler crawler = new SrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
    return new SrfSendungOverviewPageTask(crawler, input).invoke();    
  }
}
