package de.mediathekview.mserver.crawler.arte.json;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.google.gson.JsonElement;
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
public class ArteSubcategoryDeserializerTest {

  private String jsonFile;
  private Optional<String> expectedNextPage;
  private CrawlerUrlDTO[] expectedSubcategories;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/arte/arte_subcategory_page1.json",
                Optional.of("http://localhost:8589/api/opa/v3/subcategories?language=de&limit=5&page=2"),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=AUT&limit=100&page=1"),
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=AJO&limit=100&page=1"),
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=MUA&limit=100&page=1"),
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=FLM&limit=100&page=1"),
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=ENQ&limit=100&page=1")
                }
            },
            {
                "/arte/arte_subcategory_page_last.json",
                Optional.empty(),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/de/videos_subcategory/?id=AUV&limit=100&page=1")
                }
            },
            {
                "/arte/arte_subcategory_page_es.json",
                Optional.empty(),
                new CrawlerUrlDTO[]{
                    new CrawlerUrlDTO("https://www.arte.tv/guide/api/api/zones/es/videos_subcategory/?id=AUV&limit=100&page=1")
                }
            }
        });
  }

  public ArteSubcategoryDeserializerTest(final String jsonFile, Optional<String> expectedNextPage, CrawlerUrlDTO[] expectedSubcategories) {
    this.jsonFile = jsonFile;
    this.expectedNextPage = expectedNextPage;
    this.expectedSubcategories = expectedSubcategories;
  }

  @Test
  public void testDeserialize() {
    JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

    ArteSubcategoryDeserializer target = new ArteSubcategoryDeserializer();
    SendungOverviewDto actual = target.deserialize(jsonObject, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(expectedNextPage));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedSubcategories));
  }
}