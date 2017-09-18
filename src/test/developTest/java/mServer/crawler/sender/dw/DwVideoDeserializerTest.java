package mServer.crawler.sender.dw;

import com.google.gson.JsonArray;
import java.util.Arrays;
import java.util.Collection;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class DwVideoDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/dw/dw_video.json", "", "http://tv-download.dw.com/dwtv_video/flv/emd/emd20170913_gesamthd_sd_sor.mp4", "http://tv-download.dw.com/dwtv_video/flv/emd/emd20170913_gesamthd_sd_avc.mp4" },
        });
    }
    
    private final String jsonFile;
    private final String expectedUrlSmall;
    private final String expectedUrlNormal;
    private final String expectedUrlHd;
    
    public DwVideoDeserializerTest(String aJsonFile, String aUrlSmall, String aUrlNormal, String aUrlHd) {
        jsonFile = aJsonFile;
        expectedUrlSmall = aUrlSmall;
        expectedUrlNormal = aUrlNormal;
        expectedUrlHd = aUrlHd;
    }
    
    @Test
    public void testDeserialize() {
        JsonArray jsonObject = JsonFileReader.readJsonArray(jsonFile);
        
        DwVideoDeserializer target = new DwVideoDeserializer();
        DwVideoDTO actual = target.deserialize(jsonObject, DwVideoDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getUrl(Qualities.SMALL), equalTo(expectedUrlSmall));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(expectedUrlNormal));
        assertThat(actual.getUrl(Qualities.HD), equalTo(expectedUrlHd));
    }    
}