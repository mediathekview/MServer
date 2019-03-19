package de.mediathekview.mserver.crawler.orf.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.crawler.orf.OrfEpisodeInfoDTO;
import de.mediathekview.mserver.crawler.orf.OrfVideoInfoDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class OrfPlaylistDeserializerTest {

  @Test
  public void testDeserializeSingleFilm() {
    JsonElement jsonElement = JsonFileReader.readJson("/orf/orf_playlist_no_episodes.json");

    OrfPlaylistDeserializer target = new OrfPlaylistDeserializer();
    List<OrfEpisodeInfoDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.size(), equalTo(1));

    OrfEpisodeInfoDTO actualEpisode = actual.get(0);
      assertEpisode(
              actualEpisode,
        "Rede des Bundespräsidenten",
        Duration.ofSeconds(430),
        "Bundespräsident Alexander Van der Bellen zeigt sich optimistisch, wünscht sich aber, sich an das österreichische zu erinnern, also das Gemeinsame vor das Trennende zu stellen.",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q4A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q6A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q8C.mp4/playlist.m3u8",
              "https://api-tvthek.orf.at/uploads/media/subtitles/0055/75/02ea0c39f7d1f220fbc45284dd13b1d096abd5c8.ttml");
  }

  @Test
  public void testDeserializeFilmWithEpisodes() {
    JsonElement jsonElement = JsonFileReader.readJson("/orf/orf_playlist_with_episodes1.json");

    OrfPlaylistDeserializer target = new OrfPlaylistDeserializer();
    List<OrfEpisodeInfoDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.size(), equalTo(3));

      assertEpisode(
              actual.get(0),
        "ZIB 1",
        Duration.ofSeconds(1094),
        "",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q4A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q6A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q8C.mp4/playlist.m3u8",
              null);
      assertEpisode(
              actual.get(1),
        "Signation | Themen",
        Duration.ofSeconds(42),
        "",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q4A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q6A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q8C.mp4/playlist.m3u8",
              null);
      assertEpisode(
              actual.get(2),
        "Ministerrat segnet Kassenreform ab",
        Duration.ofSeconds(126),
        "Die Regierung hat im Ministerrat die Reform der Krankenkassen abgesegnet. Der Gesetzesvorschlag geht ohne große Korrekturen ins Parlament, erste Teile sollen schon ab 1. Jänner 2019 gelten.",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q4A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q6A.mp4/playlist.m3u8",
        "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q8C.mp4/playlist.m3u8",
              null);
  }

    private void assertEpisode(
            OrfEpisodeInfoDTO actualEpisode,
            String expectedTitle,
            Duration expectedDuration,
            String expectedDescription,
            String expectedUrlSmall,
            String expectedUrlNormal,
            String expectedUrlHd,
            String expectedSubtitle) {
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
