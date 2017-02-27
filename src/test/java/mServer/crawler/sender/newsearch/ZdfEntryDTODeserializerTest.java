package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZdfEntryDTODeserializerTest {
      
    @Test
    public void testDeserializeWithMissingJsonElementReturnsNull() throws IOException, URISyntaxException {
        JsonObject jsonObject = JsonFileReader.readJson("/zdf/zdf_search_page_entry_sample_missing_videocontent.json");
        
        ZDFEntryDTODeserializer target = new ZDFEntryDTODeserializer();
        ZDFEntryDTO actual = target.deserialize(jsonObject, ZDFEntryDTO.class, null);
        
        assertThat(actual, nullValue());
    }  
}
