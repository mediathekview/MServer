package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import java.util.List;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ArteCategoryDeserializerTest {
    private ArteCategoryDeserializer target;
    
    @Before
    public void testInitialize() {
        this.target = new ArteCategoryDeserializer();
    }

    @Test
    public void testDeserializeReturnsRelevantSubCategories() {
        JsonObject jsonObject = JsonFileReader.readJson("/arte/arte_category.json");        
        
        ArteCategoryDTO actual = target.deserialize(jsonObject, ArteCategoryDTO.class, null);
        assertThat(actual, notNullValue());
        
        assertThat(actual.getName(), equalTo("Kino"));
        
        List<String> actualSubCategories = actual.getSubCategories();
        assertThat(actualSubCategories, notNullValue());
        assertThat(actualSubCategories, Matchers.containsInAnyOrder("FLM", "CMG", "CMU", "MCL", "ACC"));
    }
}
