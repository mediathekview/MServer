package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
@PowerMockIgnore("javax.net.ssl.*")
public class SrFilmDetailTaskTest extends SrTaskTestBase {
 
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { 
        "https://www.sr-mediathek.de/index.php?seite=7&id=54623",
        "/sr/sr_film_page1.html",
        "/sr_player/mc.php?id=54623&tbl=&pnr=0&hd=0&devicetype=",
        "/sr/sr_film_video_details1.json",
        "Meine Traumreise",
        "Meine Traumreise vom Zürichsee an die Ostsee",
        LocalDateTime.of(2017, 9, 30, 0, 0, 0),
        Duration.of(1695, ChronoUnit.SECONDS),
        "Die Hochzeitsreise vor der Hochzeit - das ist zwar nicht die Regel, aber nicht wirklich ungewöhnlich. Speziell ist dagegen das Gefährt, das sich Anna und Thomas für ihren Trip ausgesucht haben. Die beiden reisen per Gleitschirm. Begleiten Sie das Paar vom Zürichsee zur Ostsee.",
        "",
        "https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_M.mp4",
        "https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_L.mp4",
        "https://srstorage01-a.akamaihd.net/Video/FS/MT/traumreise_20170926_124001_P.mp4"
      },
      {
        "https://www.sr-mediathek.de/index.php?seite=7&id=47720",
        "/sr/sr_film_page2_with_subtitle.html",
        "/sr_player/mc.php?id=47720&tbl=&pnr=0&hd=0&devicetype=",
        "/sr/sr_film_video_details2.json",
        "SAARTALK",
        "SAARTALK mit Annegret Kramp-Karrenbauer und Anke Rehlinger",
        LocalDateTime.of(2017, 2, 2, 0, 0, 0),
        Duration.of(2808, ChronoUnit.SECONDS),
        "Knapp zwei Monate vor der Landtagswahl sind die Spitzenkandidatinnen von CDU und SPD, Annegret Kramp-Karrenbauer und Anke Rehlinger, im SAARTALK zum einzigen direkten Schlagabtausch vor den Fernsehkameras angetreten. Dabei zeigte sich: Der größte Streitpunkt ist die Bildungspolitik. Aber auch beim Thema Investitionen gibt es Unterschiede - die vor allem im Detail liegen.",
        "https://www.sr-mediathek.de/sr_player/ut.php?file=ST_20170202.xml",
        "https://srstorage01-a.akamaihd.net/Video/FS/ST/st_2017-02-02_M.mp4",
        "https://srstorage01-a.akamaihd.net/Video/FS/ST/st_2017-02-02_L.mp4",
        ""
      }
    });
  }
  
  private final String requestUrl;
  private final String filmPageFile;
  private final String videoDetailsUrl;
  private final String videoDetailsFile;
  private final String theme;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  
  public SrFilmDetailTaskTest(final String aRequestUrl,
    final String aFilmPageFile,
    final String aVideoDetailsUrl,
    final String aVideoDetailsFile,
    final String aTheme,
    final String aExpectedTitle,
    final LocalDateTime aExpectedDate,
    final Duration aExpectedDuration,
    final String aExpectedDescription,
    final String aExpectedSubtitle,
    final String aExpectedUrlSmall,
    final String aExpectedUrlNormal,
    final String aExpectedUrlHd) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    videoDetailsUrl = aVideoDetailsUrl;
    videoDetailsFile = aVideoDetailsFile;
    theme = aTheme;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
  }
  
  @Test
  public void test() throws IOException {
    JsoupMock.mock(requestUrl, filmPageFile);
    
    setupSuccessfulJsonResponse(videoDetailsUrl, videoDetailsFile);
    
    final Set<Film> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
    
    Film actualFilm = (Film) actual.toArray()[0];
    assertThat(actualFilm, notNullValue());
    assertThat(actualFilm.getSender(), equalTo(Sender.SR));
    assertThat(actualFilm.getThema(), equalTo(theme));
    assertThat(actualFilm.getTitel(), equalTo(expectedTitle));
    assertThat(actualFilm.getTime(), equalTo(expectedDate));
    assertThat(actualFilm.getDuration(), equalTo(expectedDuration));
    assertThat(actualFilm.getBeschreibung(), equalTo(expectedDescription));
    assertThat(actualFilm.getWebsite().get().toString(), equalTo(requestUrl));

    assertThat(actualFilm.getUrl(Resolution.SMALL).toString(), equalTo(expectedUrlSmall));
    assertThat(actualFilm.getUrl(Resolution.NORMAL).toString(), equalTo(expectedUrlNormal));
    assertThat(actualFilm.hasHD(), equalTo(!expectedUrlHd.isEmpty()));
    if (!expectedUrlHd.isEmpty()) {
      assertThat(actualFilm.getUrl(Resolution.HD).toString(), equalTo(expectedUrlHd));
    }

    assertThat(actualFilm.hasUT(), equalTo(!expectedSubtitle.isEmpty()));
    if(!expectedSubtitle.isEmpty()) {
      assertThat(actualFilm.getSubtitles().toArray(new URL[0])[0].toString(), equalTo(expectedSubtitle));
    }    
  }
  
  private Set<Film> executeTask(String aTheme, String aRequestUrl) {
    return new SrFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl)).invoke();    
  }
}
