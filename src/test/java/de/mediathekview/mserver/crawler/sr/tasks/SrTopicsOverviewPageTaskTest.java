package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
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
public class SrTopicsOverviewPageTaskTest {

    private final SrTopicUrlDTO[] expectedUrls =
            new SrTopicUrlDTO[]{
                    new SrTopicUrlDTO("mag's", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MA", 1)),
                    new SrTopicUrlDTO(
                            "Medienwelt", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_ME_P", 1)),
                    new SrTopicUrlDTO(
                            "Meine Traumreise", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MT", 1)),
                    new SrTopicUrlDTO(
                            "mezz'ora italiana", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "AS_MEZI", 1)),
                    new SrTopicUrlDTO(
                            "Mit Herz am Herd", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MHAH", 1)),
                    new SrTopicUrlDTO(
                            "MusikKompass", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MK", 1)),
                    new SrTopicUrlDTO(
                            "MusikWelt", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MUW", 1)),
                    new SrTopicUrlDTO(
                            "Nachrichten in einfacher Sprache",
                            String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "NIES_A", 1)),
                    new SrTopicUrlDTO(
                            "2 Mann für alle Gänge", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "ZMANN", 1))
            };

  @Test
  public void test() throws Exception {
    SrTopicsOverviewPageTask target = new SrTopicsOverviewPageTask();

    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE, "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "def", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ghi", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "jkl", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "mno", "/sr/sr_overview_mno.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "pqr", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "stu", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "vwxyz", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ziffern", "/sr/sr_overview_09.html");

    JsoupMock.mock(urlMapping);

      ConcurrentLinkedQueue<SrTopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
