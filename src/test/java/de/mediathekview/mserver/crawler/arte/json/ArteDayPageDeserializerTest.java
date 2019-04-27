package de.mediathekview.mserver.crawler.arte.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ArteDayPageDeserializerTest {
  private final String jsonFile;
  private final ArteFilmUrlDto[] expectedFilmUrls;
  private final ArteLanguage language;

  public ArteDayPageDeserializerTest(
      final String jsonFile, final ArteLanguage language, final ArteFilmUrlDto[] expectedFilmUrls) {
    this.jsonFile = jsonFile;
    this.language = language;
    this.expectedFilmUrls = expectedFilmUrls;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/arte/arte_day_page1.json",
            ArteLanguage.DE,
            new ArteFilmUrlDto[] {
              new ArteFilmUrlDto(
                  "https://api.arte.tv/api/opa/v3/programs/de/058313-015-A",
                  "https://api.arte.tv/api/player/v1/config/de/058313-015-A?platform=ARTE_NEXT"),
              new ArteFilmUrlDto(
                  "https://api.arte.tv/api/opa/v3/programs/de/030273-710-A",
                  "https://api.arte.tv/api/player/v1/config/de/030273-710-A?platform=ARTE_NEXT"),
              new ArteFilmUrlDto(
                  "https://api.arte.tv/api/opa/v3/programs/de/085744-084-A",
                  "https://api.arte.tv/api/player/v1/config/de/085744-084-A?platform=ARTE_NEXT")
            }
          }
        });
  }

  @Test
  public void testDeserialize() {
    final JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

    final ArteDayPageDeserializer target = new ArteDayPageDeserializer(language);
    final ArteSendungOverviewDto actual = target.deserialize(jsonObject, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(Optional.empty()));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedFilmUrls));
  }
}
