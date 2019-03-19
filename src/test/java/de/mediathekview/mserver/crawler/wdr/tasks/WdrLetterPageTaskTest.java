package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.wdr.WdrCrawler;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
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
import java.util.Queue;

import static org.hamcrest.CoreMatchers.equalTo;
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
public class WdrLetterPageTaskTest {

  @Test
  public void test() {
    final Map<String, String> mapping = new HashMap<>();
    mapping.put(
        "https://www1.wdr.de/mediathek/video/sendungen-a-z/index.html",
        "/wdr/wdr_letter_page1.html");
    mapping.put(
        "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-u-102.html",
        "/wdr/wdr_letter_page2.html");
    JsoupMock.mock(mapping);

    final WdrTopicUrlDto[] expected = {
      new WdrTopicUrlDto(
          "Quarks & Co",
          "https://www1.wdr.de/mediathek/video/sendungen/quarks-und-co/index.html",
          false),
      new WdrTopicUrlDto(
          "Das Quiz für den Westen",
          "https://www1.wdr.de/mediathek/video/sendungen/das-quiz-fuer-den-westen/index.html",
          false),
      new WdrTopicUrlDto(
          "Unterhaltung",
          "https://www1.wdr.de/mediathek/video/sendungen/unterhaltung/index.html",
          false),
      new WdrTopicUrlDto(
          "Unser Westen",
          "https://www1.wdr.de/mediathek/video/sendungen/unser-westen/index.html",
          false)
    };
    final WdrCrawler crawler = Mockito.mock(WdrCrawler.class);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.WDR));
    final WdrLetterPageTask target = new WdrLetterPageTask(crawler);
    final Queue<WdrTopicUrlDto> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
