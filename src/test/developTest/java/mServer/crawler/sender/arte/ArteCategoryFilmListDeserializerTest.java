package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.JsonFileReader;
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
        return Arrays.asList(new Object[][] {   
            { "/arte/arte_films_of_category_page1.json", new String[] { "073658-000-A", "053299-141-A", "072401-061-A", "072401-059-A", "072401-060-A", "076512-000-A", "076511-000-A", "076510-000-A", "053299-140-A", "072401-058-A" } },
            { "/arte/arte_films_of_category_page_last.json", new String[] { "072401-011-A" } },
        });
    }
    
    private final String jsonFile;
    private final String[] expectedProgramIds;
    private final ArteCategoryFilmListDeserializer target;
    
    public ArteCategoryFilmListDeserializerTest(String aJsonFile, String[] aProgramIds) {
        jsonFile = aJsonFile;
        expectedProgramIds = aProgramIds;
        this.target = new ArteCategoryFilmListDeserializer();
    }
    
    @Test
    public void testDeserialize() {
        
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        ArteCategoryFilmsDTO actual = target.deserialize(jsonObject, ArteCategoryFilmsDTO.class, null);
        
        assertThat(actual, notNullValue());
        ArrayList<String> actualProgramIds = actual.getProgramIds();
        assertThat(actualProgramIds, Matchers.containsInAnyOrder(expectedProgramIds));
    }   
}
