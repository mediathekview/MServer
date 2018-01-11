package de.mediathekview.mserver.crawler.srf.parser;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.FileReader;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SrfFilmJsonDeserializerTest extends WireMockTestBase {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { "/srf/srf_film_page1.json", 
        "/srf/srf_film_page1.m3u8", 
        "/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/master.m3u8?start=0.0&end=3305.1" ,
        "1 gegen 100",
        "1 gegen 100 vom 17.05.2010",
        LocalDateTime.of(2010, 5, 17, 20, 7, 6),
        3305100,
        "Spannung pur, wenn Susanne Kunz die Frage stellt und der Kandidat zwar eine Ahnung hat aber nicht ganz sicher ist ob die Antwort stimmt. Dann wird es im Studio «1 gegen 100» ruhig und man spürt die Anspannung des Kandidaten förmlich. Nimmt er nun einen Joker zur Hilfe oder setzt er alles auf eine Karte und riskiert, ohne Geld und als Verlierer vom Platz zu gehen? Köpfchen, Mut und Taktik sind gefr\n.....",
        "https://www.srf.ch/play/tv/1-gegen-100/video/1-gegen-100-vom-17-05-2010?id=22b9dd2c-d1fd-463b-91de-d804eda74889",
        "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8?start=0.0&end=3305.1",
        "https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8?start=0.0&end=3305.1",
        "",
        ""
      },
      {
        "/srf/srf_film_page_with_subtitle.json",
        "/srf/srf_film_page_with_hd.m3u8",
        "/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/master.m3u8?start=0.0&end=333.0",
        "SRF Meteo",
        "Meteo vom 02.01.2018, 19:55",
        LocalDateTime.of(2018, 1, 2, 19, 54, 0),
        333000,
        "Meteo",
        "https://www.srf.ch/play/tv/srf-meteo/video/meteo-vom-02-01-2018-1955?id=4228f550-8702-4276-8001-03a1589804ef",
        "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_3_av.m3u8?start=0.0&end=333.0",
        "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_4_av.m3u8?start=0.0&end=333.0",
        "https://srfvodhd-vh.akamaihd.net/i/vod/meteo/2018/01/meteo_20180102_195400_8400830_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index_5_av.m3u8?start=0.0&end=333.0",
        "https://ws.srf.ch/subtitles/urn:srf:ais:video:4228f550-8702-4276-8001-03a1589804ef/subtitle.ttml"
      }
    });
  }

  private final String jsonFile;
  private final String m3u8File;
  private final String m3u8Url;
  private final String theme;
  private final String title;
  private final LocalDateTime dateTime;
  private final long duration;
  private final String description;
  private final String website;
  private final String smallUrl;
  private final String normalUrl;
  private final String hdUrl;
  private final String subtitleUrl;
  
  public SrfFilmJsonDeserializerTest(String aJsonFile, String aM3u8File, String aM3u8Url, String aTheme, String aTitle, LocalDateTime aLocalDateTime, long aDuration, String aDescription, String aWebsite, String aSmallUrl, String aNormalUrl, String aHdUrl, String aSubtitleUrl) {
    jsonFile = aJsonFile;
    m3u8File = aM3u8File;
    m3u8Url = aM3u8Url;
    theme = aTheme;
    title = aTitle;
    dateTime = aLocalDateTime;
    duration = aDuration;
    description = aDescription;
    website = aWebsite;
    smallUrl = aSmallUrl;
    normalUrl = aNormalUrl;
    hdUrl = aHdUrl;
    subtitleUrl = aSubtitleUrl;
  }
  
  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    
    setupSuccessfulResponse(m3u8Url, m3u8File);
            
    SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer();
    Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);
    
    assertThat(actual.isPresent(), equalTo(true));
    Film actualFilm = actual.get();
    assertThat(actualFilm.getSender(), equalTo(Sender.SRF));
    assertThat(actualFilm.getThema(), equalTo(theme));
    assertThat(actualFilm.getTitel(), equalTo(title));
    assertThat(actualFilm.getTime(), equalTo(dateTime));
    assertThat(actualFilm.getDuration(), equalTo(Duration.of(duration, ChronoUnit.MILLIS)));
    assertThat(actualFilm.getBeschreibung(), equalTo(description));
    assertThat(actualFilm.getWebsite().get().toString(), equalTo(website));

    assertThat(actualFilm.getUrl(Resolution.SMALL).toString(), equalTo(smallUrl));
    assertThat(actualFilm.getUrl(Resolution.NORMAL).toString(), equalTo(normalUrl));
    assertThat(actualFilm.hasHD(), equalTo(!hdUrl.isEmpty()));
    if (!hdUrl.isEmpty()) {
      assertThat(actualFilm.getUrl(Resolution.HD).toString(), equalTo(hdUrl));
    }
    
    assertThat(actualFilm.hasUT(), equalTo(!subtitleUrl.isEmpty()));
    if(!subtitleUrl.isEmpty()) {
      assertThat(actualFilm.getSubtitles().toArray(new URL[0])[0].toString(), equalTo(subtitleUrl));
    }
  }
}
