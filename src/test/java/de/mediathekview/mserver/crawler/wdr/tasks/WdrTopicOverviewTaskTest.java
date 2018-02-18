package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
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
@PowerMockIgnore("javax.net.ssl.*")
public class WdrTopicOverviewTaskTest extends WdrTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {  
      {
        3,
        "only film url",
        "http://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2430.html",
        "/wdr/wdr_film_aktuell.html",
        "",
        "",
        true,
        new String[] {
          "http://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2430.html",
        }
      },
      {
        3,
        "overview url containing only films",
        "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/index.html",
        "/wdr/wdr_topic_overview.html",
        "",
        "",
        false,
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-108.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---camping-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kaffee-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kreuzfahrt-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kueche-was-kosten-herd--co-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--100.html"
        }
      },
      {
        0,
        "overview url containing additional overview urls but no subpages configured",
        "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/index.html",
        "/wdr/wdr_topic_overview_with_child.html",
        "",
        "",
        false,
        new String[] { 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--134.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-am-samstag-240.html",
        }
      },
      {
        1,
        "overview url containing additional overview urls with subpages configured",
        "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/index.html",
        "/wdr/wdr_topic_overview_with_child.html",
        "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-beitraege-100.html",
        "/wdr/wdr_topic_overview_child_page.html",
        false,
        new String[] { 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--134.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-am-samstag-240.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-seepferdchen-pruefung-in-muelheim-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-ruhr-uni-untersucht-umarmungen-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-aussergewoehnliche-affen-portraits--100.html"
        }
      }
    });
  }
  
  private final String topic;
  private final String requestUrl;
  private final String htmlFile;
  private final String childUrl;
  private final String childHtmlFile;
  private final boolean isFileUrl;
  private final WdrTopicUrlDTO[] expectedUrls;
  
  public WdrTopicOverviewTaskTest(final int aMaxSubpages,
    final String aTopic,
    final String aRequestUrl,
    final String aHtmlFile,
    final String aChildUrl,
    final String aChildHtmlFile,
    final boolean aIsFileUrl,
    final String[] aExpectedUrls
  ) {
    rootConfig.getConfig().setMaximumSubpages(aMaxSubpages);
    
    topic = aTopic;
    requestUrl = aRequestUrl;
    htmlFile = aHtmlFile;
    isFileUrl = aIsFileUrl;
    childUrl = aChildUrl;
    childHtmlFile = aChildHtmlFile;
    
    expectedUrls = new WdrTopicUrlDTO[aExpectedUrls.length];
    for (int i = 0; i < expectedUrls.length; i++) {
      expectedUrls[i] = new WdrTopicUrlDTO(topic, aExpectedUrls[i], true);
    }
  }
  
  @Test
  public void test() throws IOException {
    Map<String, String> mapping = new HashMap<>();
    mapping.put(requestUrl, htmlFile);
    if (!childUrl.isEmpty()) {
      mapping.put(childUrl, childHtmlFile);
    }
    JsoupMock.mock(mapping);
    
    ConcurrentLinkedQueue<WdrTopicUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new WdrTopicUrlDTO(topic, requestUrl, isFileUrl));
    
    WdrTopicOverviewTask target = new WdrTopicOverviewTask(createCrawler(), urls, 0);
    Set<TopicUrlDTO> actual = target.invoke();
    
    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}