package de.mediathekview.mserver.crawler.srf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SrfFilmJsonDeserializerTest {

  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson("/srf/srf_film_page1.json");
    
    SrfFilmJsonDeserializer target = new SrfFilmJsonDeserializer();
    Optional<Film> actual = target.deserialize(jsonElement, Film.class, null);
    
    assertThat(actual.isPresent(), equalTo(true));
    Film actualFilm = actual.get();
    assertThat(actualFilm.getSender(), equalTo(Sender.SRF));
    assertThat(actualFilm.getThema(), equalTo("1 gegen 100"));
    assertThat(actualFilm.getTitel(), equalTo("1 gegen 100 vom 17.05.2010"));
    assertThat(actualFilm.getTime(), equalTo(LocalDateTime.of(2010, 5, 17, 20, 7, 6)));
    assertThat(actualFilm.getDuration(), equalTo(Duration.of(3305100, ChronoUnit.MILLIS)));
    assertThat(actualFilm.getBeschreibung(), equalTo("Spannung pur, wenn Susanne Kunz die Frage stellt und der Kandidat zwar eine Ahnung hat aber nicht ganz sicher ist ob die Antwort stimmt. Dann wird es im Studio «1 gegen 100» ruhig und man spürt die Anspannung des Kandidaten förmlich. Nimmt er nun einen Joker zur Hilfe oder setzt er alles auf eine Karte und riskiert, ohne Geld und als Verlierer vom Platz zu gehen? Köpfchen, Mut und Taktik sind gefr\n....."));
    
    assertThat(actualFilm.getUrl(Resolution.SMALL).toString(), equalTo("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_0_av.m3u8?start=0.0&end=3305.1"));
    assertThat(actualFilm.getUrl(Resolution.NORMAL).toString(), equalTo("https://hdvodsrforigin-f.akamaihd.net/i/vod/1gegen100/2010/05/1gegen100_20100517_200706_web_h264_16zu9_,lq1,mq1,hq1,.mp4.csmil/index_2_av.m3u8?start=0.0&end=3305.1"));
    assertThat(actualFilm.hasHD(), equalTo(false));
    
    // TODO geo?
    // TODO subtitle?
    // TODO assertThat(actualFilm.getWebsite(), equalTo("https://www.srf.ch/play/tv/1-gegen-100/video/17--mai-2010?id=22b9dd2c-d1fd-463b-91de-d804eda74889"));
  }
}
