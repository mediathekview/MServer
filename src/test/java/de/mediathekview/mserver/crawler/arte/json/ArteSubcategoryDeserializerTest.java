package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.arte.ArteSubcategoryUrlDto;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(Parameterized.class)
public class ArteSubcategoryDeserializerTest {

    private final String jsonFile;
    private final Optional<String> expectedNextPage;
    private final TopicUrlDTO[] expectedSubcategories;

    public ArteSubcategoryDeserializerTest(
            final String jsonFile,
            final Optional<String> expectedNextPage,
            final TopicUrlDTO[] expectedSubcategories) {
        this.jsonFile = jsonFile;
        this.expectedNextPage = expectedNextPage;
        this.expectedSubcategories = expectedSubcategories;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][]{
                    {
                            "/arte/arte_subcategory_page1.json",
                            Optional.of(
                                    "http://localhost:8589/api/opa/v3/subcategories?language=de&limit=5&page=2"),
                            new TopicUrlDTO[]{
                                    new TopicUrlDTO(
                                            "AUT",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=AUT&limit=100&page=1"),
                                    new TopicUrlDTO(
                                            "AJO",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=AJO&limit=100&page=1"),
                                    new TopicUrlDTO(
                                            "MUA",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=MUA&limit=100&page=1"),
                                    new TopicUrlDTO(
                                            "FLM",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=FLM&limit=100&page=1"),
                                    new TopicUrlDTO(
                                            "ENQ",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=ENQ&limit=100&page=1")
                            }
                    },
                    {
                            "/arte/arte_subcategory_page_last.json",
                            Optional.empty(),
                            new TopicUrlDTO[]{
                                    new TopicUrlDTO(
                                            "AUV",
                                            "https://www.arte.tv/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=AUV&limit=100&page=1")
                            }
                    },
                    {
                            "/arte/arte_subcategory_page_es.json",
                            Optional.empty(),
                            new TopicUrlDTO[]{
                                    new TopicUrlDTO(
                                            "AUV",
                                            "https://www.arte.tv/guide/api/emac/v3/es/web/zones/videos_subcategory/?id=AUV&limit=100&page=1")
                            }
                    }
        });
  }

  @Test
  public void testDeserialize() {
      final JsonElement jsonObject = JsonFileReader.readJson(jsonFile);

      final ArteSubcategoryDeserializer target = new ArteSubcategoryDeserializer();
      final ArteSubcategoryUrlDto actual = target.deserialize(jsonObject, null, null);

    assertThat(actual, notNullValue());
    assertThat(actual.getNextPageId(), equalTo(expectedNextPage));
    assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedSubcategories));
  }
}
