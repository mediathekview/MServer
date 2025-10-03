package de.mediathekview.mserver.crawler.zdf.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonObject;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfFilmDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.LocalDateTime;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class ZdfTopicSeasonDeserializerTest {
  private final ZdfTopicSeasonDeserializer target = new ZdfTopicSeasonDeserializer();

  @Test
  void deserializeValidJson() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_topic_season.json");
    final PagedElementListDTO<ZdfFilmDto> actual = target.deserialize(json, null, null);

    assertEquals(Optional.empty(), actual.getNextPage());
    assertEquals(2, actual.getElements().size());
    assertThat(
        actual.getElements(),
        Matchers.containsInAnyOrder(
            new ZdfFilmDto(
                Sender.ZDF,
                "Phuket (S36/E04)",
                "Max Parger führt seine Crew und die Gäste nach Thailand, das \"Land des Lächelns\". Ein Land voller Farben, tropischer Strände, reich verzierter Tempel und einer vielfältigen Küche.",
                "https://www.zdf.de/video/serien/das-traumschiff-104/phuket-102",
                LocalDateTime.of(2024, 3, 31, 18, 15, 0),
       //         LocalDateTime.of(2024, 3, 31, 20, 15, 0),
                "default",
                "https://api.zdf.de/tmd/2/android_native_5/vod/ptmd/mediathek/240331_2015_sendung_trs/5"),
            new ZdfFilmDto(
                Sender.ZDF,
                "Nusantara (S36/E03)",
                "Für die Reise nach Nusantara bringt Kapitän Max Parger zur Überraschung seiner Crew einen speziellen Gast mit an Bord: Veronika Bruckner.",
                "https://www.zdf.de/video/serien/das-traumschiff-104/nusantara-100",
                LocalDateTime.of(2024, 12, 2, 1, 45, 0),
//                LocalDateTime.of(2024, 1, 1, 20, 15, 0),
                "default",
                "https://api.zdf.de/tmd/2/android_native_5/vod/ptmd/mediathek/240101_2015_sendung_trs/8")));
  }

  @Test
  void deserializeJsonWithDgs() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_topic_page_with_dgs.json");
    final PagedElementListDTO<ZdfFilmDto> actual = target.deserialize(json, null, null);

    assertEquals(Optional.empty(), actual.getNextPage());
    assertEquals(2, actual.getElements().size());
    assertThat(
            actual.getElements(),
            Matchers.containsInAnyOrder(
                    new ZdfFilmDto(
                            Sender.ZDF,
                            "Die Insel der Giganten (S01/E04)",
                            "Die Knochen des Lusotitan Old Grande erzählen die Geschichte eines alten, aber ehrgeizigen Bullen, der sich aufmacht, eine Partnerin zu finden, und bereit ist, dafür alles zu riskieren.",
                            "https://www.zdf.de/video/dokus/terra-x-unter-dinos-geheimnisse-der-urzeit-dokureihe-100/terra-x-unter-dinos-lusotitan-old-grande-die-insel-der-giganten-doku-100",
                            LocalDateTime.of(2025, 8, 20, 4, 0, 0),
                            "dgs",
                            "https://api.zdf.de/tmd/2/android_native_5/vod/ptmd/mediathek/250914_dk_dinos_insel_giganten_tex_dgs/2?caption_source=250914_dk_dinos_insel_giganten_tex%2F5"),
                    new ZdfFilmDto(
                            Sender.ZDF,
                            "Die Insel der Giganten (S01/E04)",
                            "Die Knochen des Lusotitan Old Grande erzählen die Geschichte eines alten, aber ehrgeizigen Bullen, der sich aufmacht, eine Partnerin zu finden, und bereit ist, dafür alles zu riskieren.",
                            "https://www.zdf.de/video/dokus/terra-x-unter-dinos-geheimnisse-der-urzeit-dokureihe-100/terra-x-unter-dinos-lusotitan-old-grande-die-insel-der-giganten-doku-100",
                            LocalDateTime.of(2025, 8, 20, 4, 0, 0),
                            "default",
                            "https://api.zdf.de/tmd/2/android_native_5/vod/ptmd/mediathek/250914_dk_dinos_insel_giganten_tex/5")));
  }


  @Test
  void deserializeZdfNeo() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_topic_page_neo.json");
    final PagedElementListDTO<ZdfFilmDto> actual = target.deserialize(json, null, null);

    assertEquals(Optional.empty(), actual.getNextPage());
    assertEquals(13, actual.getElements().size());
    actual.getElements().forEach(element -> assertEquals(Sender.ZDF_NEO, element.getSender()));
  }
  @Test
  void deserializeNoSeason() {
    final JsonObject json = JsonFileReader.readJson("/zdf/zdf_topic_page_no_season.json");
    final PagedElementListDTO<ZdfFilmDto> actual = target.deserialize(json, null, null);

    assertEquals(Optional.empty(), actual.getNextPage());
    assertEquals(11, actual.getElements().size());
    actual.getElements().forEach(element -> assertEquals(Sender.ZDF, element.getSender()));
  }
}
