package mServer.crawler.sender.ard;

import com.google.gson.JsonObject;
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
public class ArdVideoDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/ard/ard_video.json", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874993.mp4", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874994.mp4", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874995.mp4" }
        });
    }
    
    private final String jsonFile;
    private final String expectedUrlSmall;
    private final String expectedUrlNormal;
    private final String expectedUrlHd;
    
    public ArdVideoDeserializerTest(String aJsonFile, String aUrlSmall, String aUrlNormal, String aUrlHd) {
        jsonFile = aJsonFile;
        expectedUrlSmall = aUrlSmall;
        expectedUrlNormal = aUrlNormal;
        expectedUrlHd = aUrlHd;
    }
    
    @Test
    public void testDeserialize() {
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        ArdVideoDeserializer target = new ArdVideoDeserializer();
        ArdVideoDTO actual = target.deserialize(jsonObject, ArdVideoDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getUrl(Qualities.SMALL), equalTo(expectedUrlSmall));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(expectedUrlNormal));
        assertThat(actual.getUrl(Qualities.HD), equalTo(expectedUrlHd));
    }    
}
