package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
    value = {
      "javax.net.ssl.*",
      "javax.*",
      "com.sun.*",
      "org.apache.logging.log4j.core.config.xml.*"
    })
public class OrfHistoryOverviewTaskTest {

  private final TopicUrlDTO[] expectedUrls =
      new TopicUrlDTO[] {
        new TopicUrlDTO(
            "Die Geschichte des Burgenlands",
            "https://tvthek.orf.at/history/Die-Geschichte-des-Burgenlands/9236430"),
          new TopicUrlDTO(
              "Die Geschichte Niederösterreichs",
              "https://tvthek.orf.at/history/Die-Geschichte-Niederoesterreichs/8378971"),
        new TopicUrlDTO(
            "Volksgruppen in Österreich",
            "https://tvthek.orf.at/history/Volksgruppen-in-Oesterreich/13557924")
      };

  @Test
  public void test() throws Exception {
    final OrfCrawler crawler = Mockito.mock(OrfCrawler.class);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.ORF));
    final OrfHistoryOverviewTask target = new OrfHistoryOverviewTask(crawler);

    JsoupMock.mock(OrfConstants.URL_ARCHIVE, "/orf/Orf_history_overview.html");
    final ConcurrentLinkedQueue<TopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
