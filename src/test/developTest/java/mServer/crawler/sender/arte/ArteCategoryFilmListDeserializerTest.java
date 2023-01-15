package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import mServer.test.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class ArteCategoryFilmListDeserializerTest {

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

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
            {"/arte/arte_subcategory_old_page1.json", new String[]{"078666-012-A", "078664-000-A", "080928-000-A", "074484-000-A", "074485-000-A", "079479-002-A", "080921-000-A", "082406-000-A", "072391-000-A", "080920-000-A"}, true},
            {"/arte/arte_subcategory_old_page_last.json", new String[]{"062866-009-A"}, false},
            {"/arte/arte_subcategory_page.json", new String[]{"107023-009-A","086862-000-A","107342-038-A","081587-000-A","072442-000-A"}, true}
    });
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
