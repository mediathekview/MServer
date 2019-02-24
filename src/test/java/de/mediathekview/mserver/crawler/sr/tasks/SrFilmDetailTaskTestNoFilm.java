package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
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
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
public class SrFilmDetailTaskTestNoFilm extends SrTaskTestBase {
 
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { 
        "https://www.sr-mediathek.de/index.php?seite=7&id=15808&pnr=0",
        "/sr/sr_podcast_page.html",
        "Abendrot"
      },
      { 
        "https://www.sr-mediathek.de/index.php?seite=7&id=57773",
        "/sr/sr_audio_page.html",
        "Bücherlese"
      }
    });
  }
  
  private final String requestUrl;
  private final String filmPageFile;
  private final String theme;
  
  public SrFilmDetailTaskTestNoFilm(final String aRequestUrl,
    final String aFilmPageFile,
    final String aTheme) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    theme = aTheme;
  }
  
  @Test
  public void test() throws IOException {
    JsoupMock.mock(requestUrl, filmPageFile);
    
    final Set<Film> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }
  
  private Set<Film> executeTask(String aTheme, String aRequestUrl) {
    return new SrFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl)).invoke();    
  }
}
