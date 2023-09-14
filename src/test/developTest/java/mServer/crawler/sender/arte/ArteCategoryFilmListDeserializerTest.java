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
            {"/arte/arte_category.json", new String[]{"112511-000-A", "047389-000-A", "109066-000-A", "082669-000-A", "003982-000-A", "021109-000-A"}, false},
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
