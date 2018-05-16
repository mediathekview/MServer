package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ArteCategoryFilmListDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {"/arte/arte_subcategory_page1.json", new String[]{"078666-012-A", "078664-000-A", "080928-000-A", "074484-000-A", "074485-000-A", "079479-002-A", "080921-000-A", "082406-000-A", "072391-000-A", "080920-000-A"}, true},
      {"/arte/arte_subcategory_page_last.json", new String[]{"062866-009-A"}, false},});
  }

  private final String jsonFile;
  private final String[] expectedProgramIds;
  private final boolean expectedHasNextPage;
  private final ArteCategoryFilmListDeserializer target;

  public ArteCategoryFilmListDeserializerTest(String aJsonFile, String[] aProgramIds, boolean aNextPage) {
    jsonFile = aJsonFile;
    expectedProgramIds = aProgramIds;
    expectedHasNextPage = aNextPage;
    this.target = new ArteCategoryFilmListDeserializer();
  }

  @Test
  public void testDeserialize() {

    JsonObject jsonObject = JsonFileReader.readJson(jsonFile);

    ArteCategoryFilmsDTO actual = target.deserialize(jsonObject, ArteCategoryFilmsDTO.class, null);

    assertThat(actual, notNullValue());
    assertThat(actual.hasNextPage(), equalTo(expectedHasNextPage));
    ArrayList<String> actualProgramIds = actual.getProgramIds();
    assertThat(actualProgramIds, Matchers.containsInAnyOrder(expectedProgramIds));
  }
}
