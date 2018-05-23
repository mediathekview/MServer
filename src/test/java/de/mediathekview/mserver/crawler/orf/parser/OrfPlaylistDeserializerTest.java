package de.mediathekview.mserver.crawler.orf.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.orf.OrfEpisodeInfoDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class OrfPlaylistDeserializerTest {

  @Test
  public void testDeserializeFilmWithEpisodes() {
    JsonElement jsonElement = JsonFileReader.readJson("/orf/orf_playlist_episodes.json");

    OrfPlaylistDeserializer target = new OrfPlaylistDeserializer();
    List<OrfEpisodeInfoDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.size(), equalTo(3));

    assertEpisode(actual.get(0),
        "Aktuell in Österreich",
        Duration.ofSeconds(1329),
        "",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide_episodes/online/1e4ebc47fb1b00f7cba5d47bef6a7c89/1517170556/13962830_0017_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide_episodes/online/f84e8ae67f589cb6b167f9f3617cd0e0/1517170556/13962830_0017_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide_episodes/online/38b95baea06e7f8a5760e3b80adc327f/1517170556/13962830_0017_Q8C.mp4",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/ce7c2879f6359023d5cf1ce691dd006d26c46807.ttml"
    );
    assertEpisode(actual.get(1),
        "Signation | Headlines",
        Duration.ofSeconds(47),
        "",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/30744e157f6789f34617bc3c2114770a/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9d3a9fa724a3e0615b018303e8bb558c/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/2db28f0f9086cf63b39c19165b1fc65d/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q8C.mp4",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/995219cf13e982e87924383384833f3405b74015.ttml"
    );
    assertEpisode(actual.get(2),
        "Stiwoll: Belohnung für Hinweise",
        Duration.ofSeconds(139),
        "Vor knapp drei Monaten griff ein 66-jähriger Steirer in Stiwoll zur Waffe und erschoss zwei Nachbarn. Nun setzt die Polizei eine Belohnung von 5.000 Euro für neue Hinweise aus, die zur Ergreifung dieses Mannes führen.",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9f415ec2268ec5a00196711cf52aca74/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/49990a5fb68d9c57d98f92018977fa46/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/3fccb7e7f9dfb462265d262b3267da91/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q8C.mp4",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/35fd3882d3d8b03403d60363031a8c681ad155e5.ttml"
    );
  }

  @Test
  public void testDeserializeSingleFilm() {
    JsonElement jsonElement = JsonFileReader.readJson("/orf/orf_playlist_no_episodes.json");

    OrfPlaylistDeserializer target = new OrfPlaylistDeserializer();
    List<OrfEpisodeInfoDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.size(), equalTo(1));

    OrfEpisodeInfoDTO actualEpisode = actual.get(0);
    assertEpisode(actualEpisode,
        "Bundesland heute",
        Duration.ofSeconds(30),
        "",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/0bb060c0744c962fcacca6eb9211ad70/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/4f512329a47f2cc5b196edb3170d1884/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/7fa882e42a1a23eec93f1310f302478e/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4",
        null
    );

  }

  private void assertEpisode(OrfEpisodeInfoDTO actualEpisode, String expectedTitle, Duration expectedDuration, String expectedDescription,
      String expectedUrlSmall, String expectedUrlNormal, String expectedUrlHd, String expectedSubtitle) {
    assertThat(actualEpisode.getTitle().get(), equalTo(expectedTitle));
    assertThat(actualEpisode.getDuration().get(), equalTo(expectedDuration));

    if (expectedDescription.isEmpty()) {
      assertThat(actualEpisode.getDescription().isPresent(), equalTo(false));
    } else {
      assertThat(actualEpisode.getDescription().get(), equalTo(expectedDescription));
    }

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
