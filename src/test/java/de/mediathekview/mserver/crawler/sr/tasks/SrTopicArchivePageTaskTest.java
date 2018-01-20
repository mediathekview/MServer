package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class SrTopicArchivePageTaskTest {

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
  
  @Test
  public void testOverviewWithSinglePage() throws IOException {
    CrawlerUrlDTO[] expectedUrls = new CrawlerUrlDTO[] { 
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=49674"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=49442"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=49171"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=48954")
    };

    String requestUrl = "srf_sample.html";
    JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_single.html");
    
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }  
  
  @Test
  public void testOverviewWithMultiplePages() throws IOException {
    CrawlerUrlDTO[] expectedUrls = new CrawlerUrlDTO[] { 
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=54623"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=54536"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=54310"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=54078"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=53895"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=52595"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=52317"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=51814"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=51668"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=33014"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=51200"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=44118"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=49170"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=48941"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=48761"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=48574"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=38815"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=47765"),
      new CrawlerUrlDTO("https://www.sr-mediathek.de/index.php?seite=7&id=47554")
    };

    String requestUrl = "https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=1";
    
    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl, "/sr/sr_sendung_overview_page1.html");
    urlMapping.put("https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=2", "/sr/sr_sendung_overview_page2.html");
    JsoupMock.mock(urlMapping);
    
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }  
  
  @Test
  public void testOverviewEmpty() throws IOException {
    CrawlerUrlDTO[] expectedUrls = new CrawlerUrlDTO[0];

    String requestUrl = "srf_sample.html";
    JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_empty.html");
    
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }  

  private Set<CrawlerUrlDTO> executeTask(String aRequestUrl) {
    return new SrTopicArchivePageTask(createCrawler(), createCrawlerUrlDto(aRequestUrl)).invoke();    
  }
  
  protected SrCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    
    return new SrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  } 
  
  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(String aUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO(aUrl));
    return input;
  }
  
}
