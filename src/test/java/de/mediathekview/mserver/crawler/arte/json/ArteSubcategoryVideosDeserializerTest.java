package de.mediathekview.mserver.crawler.arte.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.SendungOverviewDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ArteSubcategoryVideosDeserializerTest {

  private final String jsonFile;
  private ArteLanguage language;
  private final Optional<String> expectedNextPage;
  private final CrawlerUrlDTO[] expectedSubcategories;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/arte/arte_subcategory_films_page1.json",
                ArteLanguage.DE,
                Optional.of("http://localhost:8589/api/emac/v3/de/web/zones/videos_subcategory?id=ART&page=2&limit=5"),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/084733-002-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086780-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086779-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086777-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/083949-027-A"),
                }
            },
            {
                "/arte/arte_subcategory_films_page_last.json",
                ArteLanguage.DE,
                Optional.empty(),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086778-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086775-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086773-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086776-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/de/086774-000-A"),
                }
            },
            {
                "/arte/arte_subcategory_films_pl.json",
                ArteLanguage.PL,
                Optional.empty(),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/pl/065344-000-A"),
                    new CrawlerUrlDTO("https://api.arte.tv/api/opa/v3/programs/pl/059265-013-A")
                }
            }

        });
  }

  public ArteSubcategoryVideosDeserializerTest(final String jsonFile, final ArteLanguage language, Optional<String> expectedNextPage,
      CrawlerUrlDTO[] expectedSubcategories) {
    this.jsonFile = jsonFile;
    this.language = language;
    this.expectedNextPage = expectedNextPage;
    this.expectedSubcategories = expectedSubcategories;
  }

  @Test
  public void testDeserialize() {
    JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

    ArteSubcategoryVideosDeserializer target = new ArteSubcategoryVideosDeserializer(language);
    SendungOverviewDto actual = target.deserialize(jsonObject, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(expectedNextPage));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedSubcategories));
  }
}