package mServer.crawler.sender.ard;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collection;
import de.mediathekview.mlib.daten.Qualities;
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
            { "/ard/ard_video_without_hd.json", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874993.mp4", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/147/1471174/1471174_16874995.mp4", null },
            { "/ard/ard_video_with_hd.json", "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-e9ebd6e42ce1_14.mp4", "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-c7cca1d51b4b_14.mp4", "http://ondemand.mdr.de/mp4dyn/1/FCMS-14ec1b13-c7f6-4cc4-8b72-681cd22da39a-15e7604ea4a4_14.mp4" },
            { "/ard/ard_video_normal_use_last.json", "https://mediastorage01.sr-online.de/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_M.mp4", "https://srstorage01-a.akamaihd.net/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_L.mp4", "https://srstorage01-a.akamaihd.net/Video/UD/DOKU/1505155201_20170911_KANDIDATENCHECK_LUKSIC_P.mp4" },
            { "/ard/ard_video_use_http_url.json", "http://cdn-storage.br.de/iLCpbHJGNL9zu6i6NL97bmWH_-bf/_-0S/_Abg5-xg5U1S/0f131ba9-c8e1-4368-be7b-799a75df221f_2.mp3", null, null },
            { "/ard/ard_video_with_quality_3_no_hd.json", "http://pd-videos.daserste.de/int/2017/09/08/7f35d2d7-9854-4187-b406-c56b9292de79/512-1.mp4", "http://pd-videos.daserste.de/int/2017/09/08/7f35d2d7-9854-4187-b406-c56b9292de79/960-1.mp4", null }
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
