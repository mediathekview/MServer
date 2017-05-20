package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;

public class ArteStaticContentDeserializerTest {

    private ArteStaticContentDeserializer target;
    
    @Before
    public void testInitialize() {
        this.target = new ArteStaticContentDeserializer();
    }

    @Test
    public void testDeserializeReturnsDtoWithCategories() {
        JsonObject jsonObject = JsonFileReader.readJson("/arte/arte_static_content.json");        
        
        ArteInfoDTO actual = target.deserialize(jsonObject, ArteInfoDTO.class, null);
        assertThat(actual, notNullValue());
        
        Iterable<String> actualCategories = actual.getCategories();
        assertThat(actualCategories, notNullValue());
        assertThat(actualCategories, Matchers.containsInAnyOrder("Aktuelles und Gesellschaft", "Kino", "Fernsehfilme und Serien", "Kultur und Pop", "ARTE Concert", "Wissenschaft", "Entdeckung der Welt", "Geschichte"));

        assertThat(actual.getCategoryUrl("Aktuelles und Gesellschaft"), equalTo("http://www.arte.tv/de/videos/aktuelles-und-gesellschaft"));
        assertThat(actual.getCategoryUrl("Kultur und Pop"), equalTo("http://www.arte.tv/de/videos/kultur-und-pop"));
        assertThat(actual.getCategoryUrl("Geschichte"), equalTo("http://www.arte.tv/de/videos/geschichte"));
    }
}
