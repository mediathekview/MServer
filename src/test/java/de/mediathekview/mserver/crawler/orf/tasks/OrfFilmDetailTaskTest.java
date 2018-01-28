package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
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
public class OrfFilmDetailTaskTest {
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { 
        "http://tvthek.orf.at/profile/100-Jahre-Simpl/13888311/100-Jahre-Simpl/13962984",
        "/orf/orf_film1.html",
        "100 Jahre Simpl",
        "100 Jahre Simpl",
        LocalDateTime.of(2018, 1, 28, 0, 0, 0),
        Duration.of(2658, ChronoUnit.SECONDS),
        "Das Beste aus dem Simpl der letzten 50 Jahre aus der Ära Michael Niavarani, Albert Schmidtleitner Mit Christoph Fälbl, Roman Frankl, Vikor Gernot, Sigrid Hauser, Michael A. Mohapp, Bernhard Murg, Michael Niavarani, Steffi Paschke, Alexandra Schmid, Bettina Soriat, Herbert Steinböck u.v.a.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/32/b57f2d8b019f1f0ad3471d85226c123db557b831.ttml",
        "http://apasfpd.apa.at/cms-worldwide/online/446dc6484f90d21fdc2614274f3f7126/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q4A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/0b0004b823a3c06c9d44e2b68868f1cf/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q6A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/19ebe3c3bb148b8e8251eca0b7cae0c8/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q8C.mp4"
      }
    });
  }
  
  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
  
  private final String requestUrl;
  private final String filmPageFile;
  private final String theme;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  
  public OrfFilmDetailTaskTest(final String aRequestUrl,
    final String aFilmPageFile,
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
    
    final Set<Film> actual = executeTask(theme, requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
    
    Film actualFilm = (Film) actual.toArray()[0];
    assertThat(actualFilm, notNullValue());
    assertThat(actualFilm.getSender(), equalTo(Sender.ORF));
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
    return new OrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl)).invoke();    
  }
  
  private OrfCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new OrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }  

  private ConcurrentLinkedQueue<OrfTopicUrlDTO> createCrawlerUrlDto(String aTheme, String aUrl) {
    ConcurrentLinkedQueue<OrfTopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new OrfTopicUrlDTO(aTheme, aUrl));
    return input;
  }    
}
