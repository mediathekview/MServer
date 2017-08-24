package mServer.developTest.crawler.sender.newsearch;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.gson.JsonObject;

import mServer.crawler.sender.zdf.ZDFEntryDTO;
import mServer.crawler.sender.zdf.ZDFEntryDTODeserializer;
import mServer.developTest.test.JsonFileReader;

public class ZdfEntryDTODeserializerTest
{

    @Test
    public void testDeserializeWithMissingJsonElementReturnsNull()
    {
        final JsonObject jsonObject =
                JsonFileReader.readJson("/zdf/zdf_search_page_entry_sample_missing_videocontent.json");

        final ZDFEntryDTODeserializer target = new ZDFEntryDTODeserializer();
        final ZDFEntryDTO actual = target.deserialize(jsonObject, ZDFEntryDTO.class, null);

        assertThat(actual, nullValue());
    }
}
