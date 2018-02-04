package de.mediathekview.mserver.crawler.orf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.orf.OrfEpisodeInfoDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class OrfEpisodeDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      {
        "/orf/orf_episode.json",
        "Stiwoll: Belohnung für Hinweise",
        Duration.of(139, ChronoUnit.SECONDS),
        "Vor knapp drei Monaten griff ein 66-jähriger Steirer in Stiwoll zur Waffe und erschoss zwei Nachbarn. Nun setzt die Polizei eine Belohnung von 5.000 Euro für neue Hinweise aus, die zur Ergreifung dieses Mannes führen.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/35fd3882d3d8b03403d60363031a8c681ad155e5.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9f415ec2268ec5a00196711cf52aca74/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/49990a5fb68d9c57d98f92018977fa46/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/3fccb7e7f9dfb462265d262b3267da91/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q8C.mp4"
      }      
    });
  }
  
  private final String jsonFile;
  private final String expectedTitle;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  
  public OrfEpisodeDeserializerTest(final String aJsonFile,
    final String aExpectedTitle,
    final Duration aExpectedDuration,
    final String aExpectedDescription,
    final String aExpectedSubtitle,
    final String aExpectedUrlSmall,
    final String aExpectedUrlNormal,
    final String aExpectedUrlHd) {
    jsonFile = aJsonFile;
    expectedTitle = aExpectedTitle;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
  }
  
  @Test
  public void test() {
    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);
    
    OrfEpisodeDeserializer target = new OrfEpisodeDeserializer();
    Optional<OrfEpisodeInfoDTO> actual = target.deserialize(jsonElement, null, null);
    
    assertThat(actual.isPresent(), equalTo(true));
    OrfEpisodeInfoDTO actualEpisode = actual.get();
    assertThat(actualEpisode.getTitle().get(), equalTo(expectedTitle));
    assertThat(actualEpisode.getDuration().get(), equalTo(expectedDuration));
    assertThat(actualEpisode.getDescription().get(), equalTo(expectedDescription));
    
    OrfVideoInfoDTO actualVideoInfo = actualEpisode.getVideoInfo();
    assertThat(actualVideoInfo, notNullValue());
    
    Map<Resolution, String> actualVideoUrls = actualVideoInfo.getVideoUrls();
    assertThat(actualVideoUrls.get(Resolution.SMALL), equalTo(expectedUrlSmall));
    assertThat(actualVideoUrls.get(Resolution.NORMAL), equalTo(expectedUrlNormal));
    assertThat(actualVideoUrls.containsKey(Resolution.HD), equalTo(!expectedUrlHd.isEmpty()));
    if (!expectedUrlHd.isEmpty()) {
      assertThat(actualVideoUrls.get(Resolution.HD), equalTo(expectedUrlHd));
    }

    assertThat(actualVideoInfo.getSubtitleUrl(), equalTo(expectedSubtitle));
  }
}
