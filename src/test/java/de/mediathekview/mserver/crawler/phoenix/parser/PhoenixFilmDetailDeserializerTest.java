package de.mediathekview.mserver.crawler.phoenix.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PhoenixFilmDetailDeserializerTest {

    public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/phoenix/phoenix_film_detail1.json",
            "Presseclub",
            "Mehr Grenzschutz und eine neue Asylpolitik – letzte Rettung für Europa und Merkel?",
            "Moderation: Sonia Seymour Mikich",
            "293872",
            "https://www.phoenix.de/sendungen/gespraeche/presseclub/mehr-grenzschutz-und-eine-neue-asylpolitik--letzte-rettung-fuer-europa-und-merkel-a-271252.html"
          },
          {
            "/phoenix/phoenix_film_detail_title_contains_not_topic.json",
            "Dokumentationen",
            "Ungezähmtes Albanien",
            "Film von Barbara Fally-Puskás, ORF ",
            "3030967",
            "https://www.phoenix.de/sendungen/dokumentationen/ungezaehmtes-albanien-a-2081815.html"
          },
          {
            "/phoenix/phoenix_film_detail_title_and_subtitle.json",
            "Dokumentationen",
            "The Wall - Mauern der Welt - Nordirlands \"Friedenslinien\"",
            "Film von Caryl Ebenezer, phoenix 2022",
            "2752659",
            "https://www.phoenix.de/sendungen/dokumentationen/the-wall---mauern-der-wel-a-2642819.html"
          }
        });
    }

  @MethodSource("data")
  @ParameterizedTest
  void test(final String jsonFile, final String expectedTopic, final String expectedTitle, final String expectedDescription, final String expectedBaseName, final String expectedWebsite) {
      final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

      final PhoenixFilmDetailDeserializer target = new PhoenixFilmDetailDeserializer();
      final Optional<PhoenixFilmDetailDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
      final PhoenixFilmDetailDto actualDto = actual.get();

    assertThat(actualDto.getBaseName(), equalTo(expectedBaseName));
    assertThat(actualDto.getTopic(), equalTo(expectedTopic));
    assertThat(actualDto.getTitle(), equalTo(expectedTitle));
    assertThat(actualDto.getDescription(), equalTo(expectedDescription));
    assertThat(actualDto.getWebsite().get(), equalTo(expectedWebsite));
  }
}
