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
public class OrfArchiveLetterPageTaskTest {

  private final TopicUrlDTO[] expectedUrls =
      new TopicUrlDTO[] {
        new TopicUrlDTO(
            "Best of \"ZIB 2\"-Interviews",
            "http://tvthek.orf.at/archive/Best-of-ZIB-2-Interviews/7874678"),
        new TopicUrlDTO(
            "Bundesheer-Archiv", "http://tvthek.orf.at/archive/Bundesheer-Archiv/5106911"),
        new TopicUrlDTO(
            "Bundespräsidentenwahlen in Österreich",
            "http://tvthek.orf.at/archive/Bundespraesidentenwahlen-in-Oesterreich/13304953"),
        new TopicUrlDTO(
            "Bundestagswahl-Archiv", "http://tvthek.orf.at/archive/Bundestagswahl-Archiv/6524565"),
        new TopicUrlDTO(
            "Die Geschichte des Burgenlands",
            "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430"),
        new TopicUrlDTO(
            "Die politische Geschichte der Zweiten Republik",
            "http://tvthek.orf.at/archive/Die-politische-Geschichte-der-Zweiten-Republik/9501692")
      };

  @Test
  public void test() throws Exception {
    final OrfCrawler crawler = Mockito.mock(OrfCrawler.class);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.ORF));
    final OrfArchiveLetterPageTask target = new OrfArchiveLetterPageTask(crawler);

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(OrfConstants.URL_ARCHIVE, "/orf/orf_archive_letter_multiple_themes.html");
    urlMapping.put(
        OrfConstants.URL_ARCHIVE + "/letter/B", "/orf/orf_archive_letter_multiple_themes.html");
    urlMapping.put(
        OrfConstants.URL_ARCHIVE + "/letter/Z", "/orf/orf_archive_letter_single_theme.html");

    JsoupMock.mock(urlMapping);
    final ConcurrentLinkedQueue<TopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
