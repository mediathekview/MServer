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
public class ArteCategoryFilmListDeserializerTest {

  private final String jsonFile;
  private final String[] expectedProgramIds;
  private final boolean expectedHasNextPage;
  private final String expectedNextPageUrl;
  private final ArteCategoryFilmListDeserializer target;
  public ArteCategoryFilmListDeserializerTest(String aJsonFile, String[] aProgramIds, boolean aNextPage, String nextPageUrl) {
    jsonFile = aJsonFile;
    expectedProgramIds = aProgramIds;
    expectedHasNextPage = aNextPage;
    expectedNextPageUrl = nextPageUrl;
    this.target = new ArteCategoryFilmListDeserializer();
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"/arte/arte_category.json", new String[]{"112511-000-A", "047389-000-A", "109066-000-A", "082669-000-A", "003982-000-A", "021109-000-A"}, false, null},
            {"/arte/arte_video_list1.json", new String[]{"033559-000-A","078154-000-A", "101398-000-A", "109332-000-A", "111063-000-A"}, true, "https://www.arte.tv/api/rproxy/emac/v4/de/web/zones/daeadc71-4306-411a-8590-1c1f484ef5aa/content?abv=B&authorizedCountry=DE&page=2&pageId=MOST_RECENT&zoneIndexInPage=0"}
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
