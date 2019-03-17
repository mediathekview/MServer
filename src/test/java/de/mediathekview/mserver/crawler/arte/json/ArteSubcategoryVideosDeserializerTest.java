package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ArteSubcategoryVideosDeserializerTest {

  private final String jsonFile;
  private final Optional<String> expectedNextPage;
  private final ArteFilmUrlDto[] expectedSubcategories;
    private final ArteLanguage language;

    public ArteSubcategoryVideosDeserializerTest(
            final String jsonFile,
            final ArteLanguage language,
            final Optional<String> expectedNextPage,
            final ArteFilmUrlDto[] expectedSubcategories) {
        this.jsonFile = jsonFile;
        this.language = language;
        this.expectedNextPage = expectedNextPage;
        this.expectedSubcategories = expectedSubcategories;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][]{
                    {
                            "/arte/arte_subcategory_films_page1.json",
                            ArteLanguage.DE,
                            Optional.of(
                                    "http://localhost:8589/api/emac/v3/de/web/zones/videos_subcategory?id=ART&page=2&limit=5"),
                            new ArteFilmUrlDto[]{
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/084733-002-A",
                                            "https://api.arte.tv/api/player/v1/config/de/084733-002-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086780-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086780-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086779-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086779-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086777-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086777-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/083949-027-A",
                                            "https://api.arte.tv/api/player/v1/config/de/083949-027-A?platform=ARTE_NEXT"),
                            }
                    },
                    {
                            "/arte/arte_subcategory_films_page_last.json",
                            ArteLanguage.DE,
                            Optional.empty(),
                            new ArteFilmUrlDto[]{
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086778-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086778-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086775-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086775-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086773-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086773-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086776-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086776-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/de/086774-000-A",
                                            "https://api.arte.tv/api/player/v1/config/de/086774-000-A?platform=ARTE_NEXT"),
                            }
                    },
                    {
                            "/arte/arte_subcategory_films_pl.json",
                            ArteLanguage.PL,
                            Optional.empty(),
                            new ArteFilmUrlDto[]{
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/pl/065344-000-A",
                                            "https://api.arte.tv/api/player/v1/config/pl/065344-000-A?platform=ARTE_NEXT"),
                                    new ArteFilmUrlDto(
                                            "https://api.arte.tv/api/opa/v3/programs/pl/059265-013-A",
                                            "https://api.arte.tv/api/player/v1/config/pl/059265-013-A?platform=ARTE_NEXT")
                            }
                    }
        });
  }

  @Test
  public void testDeserialize() {
      final JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

      final ArteSubcategoryVideosDeserializer target = new ArteSubcategoryVideosDeserializer(language);
      final ArteSendungOverviewDto actual = target.deserialize(jsonObject, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(expectedNextPage));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedSubcategories));
  }
}
