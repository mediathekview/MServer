package de.mediathekview.mserver.crawler.phoenix.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class PhoenixFilmDetailDeserializerTest {

  private final String jsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final String expectedBaseName;
  private final String expectedWebsite;

    public PhoenixFilmDetailDeserializerTest(
            final String aJsonFile,
            final String aExpectedTopic,
            final String aExpectedTitle,
            final String aExpectedDescription,
            final String aExpectedBaseName,
            final String aExpectedWebsite) {
    jsonFile = aJsonFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedBaseName = aExpectedBaseName;
    expectedWebsite = aExpectedWebsite;
  }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][]{
                        {
                                "/phoenix/phoenix_film_detail1.json",
                                "Presseclub",
                                "Mehr Grenzschutz und eine neue Asylpolitik – letzte Rettung für Europa und Merkel?",
                                "Moderation: Sonia Seymour Mikich",
                                "293872",
                                "https://www.phoenix.de/sendungen/gespraeche/presseclub/mehr-grenzschutz-und-eine-neue-asylpolitik--letzte-rettung-fuer-europa-und-merkel-a-271252.html"
                        }
                });
    }

  @Test
  public void test() {
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
