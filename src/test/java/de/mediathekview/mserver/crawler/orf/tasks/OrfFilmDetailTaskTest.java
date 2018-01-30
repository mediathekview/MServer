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
        "/orf/orf_film_with_subtitle.html",
        "100 Jahre Simpl",
        "100 Jahre Simpl",
        LocalDateTime.of(2018, 1, 28, 0, 0, 0),
        Duration.of(2658, ChronoUnit.SECONDS),
        "Das Beste aus dem Simpl der letzten 50 Jahre aus der Ära Michael Niavarani, Albert Schmidtleitner Mit Christoph Fälbl, Roman Frankl, Vikor Gernot, Sigrid Hauser, Michael A. Mohapp, Bernhard Murg, Michael Niavarani, Steffi Paschke, Alexandra Schmid, Bettina Soriat, Herbert Steinböck u.v.a.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/32/b57f2d8b019f1f0ad3471d85226c123db557b831.ttml",
        "http://apasfpd.apa.at/cms-worldwide/online/446dc6484f90d21fdc2614274f3f7126/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q4A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/0b0004b823a3c06c9d44e2b68868f1cf/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q6A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/19ebe3c3bb148b8e8251eca0b7cae0c8/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q8C.mp4"
      },
      {
        "http://tvthek.orf.at/profile/Mountain-Attack/13886812/Mountain-Attack-Highlights-aus-Saalbach/13962229",
        "/orf/orf_film_no_subtitle.html",
        "Mountain Attack, Highlights aus Saalbach",
        "Mountain Attack, Highlights aus Saalbach",
        LocalDateTime.of(2018, 1,22, 19, 30, 00),
        Duration.of(1013, ChronoUnit.SECONDS),
        "Bei der 20. Mountain Attack im Jänner 2018 bezwangen die Tourenskisportler sechs Gipfel und 3.008 Höhenmeter auf einer Strecke von 40 Kilometern.",
        "",
        "http://apasfpd.apa.at/cms-austria/online/b81830be5e344d34259b9cb8c747977f/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q4A.mp4",
        "http://apasfpd.apa.at/cms-austria/online/e959ba55a87acd553e04296c196fd079/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q6A.mp4",
        "http://apasfpd.apa.at/cms-austria/online/12a33dab839ecd322a3440600a147f31/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q8C.mp4"
      },
      {
        "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962830",
        "/orf/orf_film_with_several_parts.html",
        "Aktuell in Österreich",
        "Aktuell in Österreich",
        LocalDateTime.of(2018, 1, 26, 17, 5, 0),
        Duration.of(1329, ChronoUnit.SECONDS),
        "",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/995219cf13e982e87924383384833f3405b74015.ttml",
        "http://apasfpd.apa.at/cms-worldwide/online/30744e157f6789f34617bc3c2114770a/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q4A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/9d3a9fa724a3e0615b018303e8bb558c/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q6A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/2db28f0f9086cf63b39c19165b1fc65d/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q8C.mp4"
      },
      {
        "http://tvthek.orf.at/profile/Das-ewige-Leben/13886855/Das-ewige-Leben/13963129",
        "/orf/orf_film_duration_hour.html",
        "Das ewige Leben",
        "Das ewige Leben",
        LocalDateTime.of(2018, 1, 27, 20, 15, 5),
        Duration.of(113, ChronoUnit.MINUTES),
        "Hinweis der Redaktion: Aus Gründen des Jugendschutzes ist diese Sendung nur zwischen 20.00 Uhr und 6.00 Uhr als Video on demand abrufbar. Brenner kehrt nach Graz zurück, in die Stadt seiner Jugend. In der Konfrontation mit seinen Jugendfreunden, seiner Jugendliebe und seiner großen Jugendsünde, kommt es zu Morden und zu einem verhängnisvollen Kopfschuss.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/30/9518d4ae312b0c9e7484cf9f90e466ab826d4d40.ttml",
        "http://apasfpd.apa.at/cms-austria/online/992ac08cde7be11fac6e50b3510f3fe6/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q4A.mp4",
        "http://apasfpd.apa.at/cms-austria/online/f74cb1dbce56813f59cd66999e9d3338/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q6A.mp4",
        "http://apasfpd.apa.at/cms-austria/online/4f6df57f3acdfcaa2fe8e914699186ce/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q8C.mp4"
      },
      {
        "http://tvthek.orf.at/profile/BUNDESLAND-HEUTE/8461416/Bundesland-heute/13890700",
        "/orf/orf_film_date_cest.html",
        "Bundesland heute",
        "Bundesland heute",
        LocalDateTime.of(2016, 10, 1, 12, 55, 9),
        Duration.of(30, ChronoUnit.SECONDS),
        "",
        "",
        "http://apasfpd.apa.at/cms-worldwide/online/0bb060c0744c962fcacca6eb9211ad70/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/4f512329a47f2cc5b196edb3170d1884/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/7fa882e42a1a23eec93f1310f302478e/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4"
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
