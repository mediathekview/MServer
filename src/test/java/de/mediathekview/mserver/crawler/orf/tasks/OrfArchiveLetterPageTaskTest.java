package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
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
public class OrfArchiveLetterPageTaskTest {

  private final OrfTopicUrlDTO[] expectedUrls= new OrfTopicUrlDTO[] {
    new OrfTopicUrlDTO("Best of \"ZIB 2\"-Interviews", "http://tvthek.orf.at/archive/Best-of-ZIB-2-Interviews/7874678"),
    new OrfTopicUrlDTO("Bundesheer-Archiv", "http://tvthek.orf.at/archive/Bundesheer-Archiv/5106911"),
    new OrfTopicUrlDTO("Bundespräsidentenwahlen in Österreich", "http://tvthek.orf.at/archive/Bundespraesidentenwahlen-in-Oesterreich/13304953"),
    new OrfTopicUrlDTO("Bundestagswahl-Archiv", "http://tvthek.orf.at/archive/Bundestagswahl-Archiv/6524565"),
    new OrfTopicUrlDTO("Die Geschichte des Burgenlands", "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430"),
    new OrfTopicUrlDTO("Die politische Geschichte der Zweiten Republik", "http://tvthek.orf.at/archive/Die-politische-Geschichte-der-Zweiten-Republik/9501692")
  };
  
  @Test
  public void test() throws Exception {
    OrfArchiveLetterPageTask target = new OrfArchiveLetterPageTask();

    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(OrfConstants.URL_ARCHIVE, "/orf/orf_archive_letter_multiple_themes.html");
    urlMapping.put(OrfConstants.URL_ARCHIVE + "/letter/B", "/orf/orf_archive_letter_multiple_themes.html");
    urlMapping.put(OrfConstants.URL_ARCHIVE + "/letter/Z", "/orf/orf_archive_letter_single_theme.html");

    JsoupMock.mock(urlMapping);
    ConcurrentLinkedQueue<OrfTopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
