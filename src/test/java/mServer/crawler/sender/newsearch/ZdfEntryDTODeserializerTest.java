package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZdfEntryDTODeserializerTest {
      
    @Test
    public void testDeserializeWithMissingJsonElementReturnsNull() {
        JsonObject jsonObject = JsonFileReader.readJson("/zdf/zdf_search_page_entry_sample_missing_videocontent.json");
        
        ZDFEntryDTODeserializer target = new ZDFEntryDTODeserializer();
        ZDFEntryDTO actual = target.deserialize(jsonObject, ZDFEntryDTO.class, null);
        
        assertThat(actual, nullValue());
    }  
}
