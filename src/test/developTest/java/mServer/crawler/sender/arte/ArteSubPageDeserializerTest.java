package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import mServer.test.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ArteSubPageDeserializerTest {

  private final String jsonFile;
  private final String[] expectedProgramIds;
  private final boolean expectedHasNextPage;
  private final String expectedNextPageUrl;
  private final ArteSubPageDeserializer target;
  public ArteSubPageDeserializerTest(String aJsonFile, String[] aProgramIds, boolean aNextPage, String nextPageUrl) {
    jsonFile = aJsonFile;
    expectedProgramIds = aProgramIds;
    expectedHasNextPage = aNextPage;
    expectedNextPageUrl = nextPageUrl;
    this.target = new ArteSubPageDeserializer();
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"/arte/arte_video_list2.json", new String[]{"099708-000-A", "098846-000-A", "111648-001-A", "112235-000-A", "113043-139-A"}, true, "https://www.arte.tv/api/rproxy/emac/v4/de/web/zones/82b597d7-a83b-4dd8-bea8-ad71675fdf23/content?abv=A&authorizedCountry=DE&page=3&pageId=MOST_VIEWED&zoneIndexInPage=0"},
            {"/arte/arte_video_list_last.json", new String[]{"102805-000-A","104017-000-A", "106273-006-A"}, false, null}
    });
  }

  @Test
  public void testDeserialize() {

    JsonObject jsonObject = JsonFileReader.readJson(jsonFile);

    ArteCategoryFilmsDTO actual = target.deserialize(jsonObject, ArteCategoryFilmsDTO.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.hasNextPage(), equalTo(expectedHasNextPage));
    Set<String> actualProgramIds = actual.getProgramIds();
    assertThat(actualProgramIds, Matchers.containsInAnyOrder(expectedProgramIds));
    assertThat(actual.getNextPageUrl(), equalTo(expectedNextPageUrl));
  }
}
