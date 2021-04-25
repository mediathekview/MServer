package de.mediathekview.mserver.crawler.ard.json.livestream;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.livestream.json.LivestreamArdDeserializer;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class LivesreamArdDeserializerTest {

  @Test
  public void test() {
    final TopicUrlDTO[] expectedUrls =
        new TopicUrlDTO[] {
            new TopicUrlDTO(
                "ARTE Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21200502"),
                new TopicUrlDTO(
                "Deutsche Welle (DW) - Die mediale Stimme Deutschlands",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/35283082"),
                new TopicUrlDTO(
                "rbb Fernsehen Berlin",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/8256334"),
                new TopicUrlDTO(
                "MDR Fernsehen Thüringen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/79556518"),
                new TopicUrlDTO(
                "BR Fernsehen Nord",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21534364"),
                new TopicUrlDTO(
                "NDR Fernsehen Schleswig-Holstein",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/68615964"),
                new TopicUrlDTO(
                "3sat Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/24124076"),
                new TopicUrlDTO(
                "ARD-alpha Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21542412"),
                new TopicUrlDTO(
                "SR Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/22496138"),
                new TopicUrlDTO(
                "SWR Fernsehen Rheinland-Pfalz",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/78097946"),
                new TopicUrlDTO(
                "ONE im Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/3540670"),
                new TopicUrlDTO(
                "MDR Fernsehen Sachsen-Anhalt",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/79556532"),
                new TopicUrlDTO(
                "NDR Fernsehen Mecklenburg-Vorpommern",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/68615988"),
                new TopicUrlDTO(
                "BR Fernsehen Süd",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21534364"),
                new TopicUrlDTO(
                "SWR Fernsehen Baden-Württemberg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/78097938"),
                new TopicUrlDTO(
                "NDR Fernsehen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/68586106"),
                new TopicUrlDTO(
                "ARTE Livestream französisch",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21200502"),
                new TopicUrlDTO(
                "WDR Fernsehen im Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/28849966"),
                new TopicUrlDTO(
                "tagesschau24-Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/31493180"),
                new TopicUrlDTO(
                "KiKA Livestream",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/29026018"),
                new TopicUrlDTO(
                "phoenix live",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/83019244"),
                new TopicUrlDTO(
                "Das Erste",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/21520934"),
                new TopicUrlDTO(
                "NDR Fernsehen Hamburg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/68615936"),
                new TopicUrlDTO(
                "MDR Fernsehen Sachsen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/79556546"),
                new TopicUrlDTO(
                "hr-fernsehen",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/17301400"),
                new TopicUrlDTO(
                "rbb Fernsehen Brandenburg",
                "https://appdata.ardmediathek.de/appdata/servlet/play/config/8256334")
        };

    final JsonElement jsonElement = JsonFileReader.readJson("/livestream/livestream_ard_overview.json");

    final LivestreamArdDeserializer target = new LivestreamArdDeserializer();
    final Set<TopicUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    assertEquals(actual.size(), expectedUrls.length);
  }
}
