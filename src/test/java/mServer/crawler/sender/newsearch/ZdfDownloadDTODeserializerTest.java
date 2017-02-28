package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ZdfDownloadDTODeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {   
            { "/zdf/zdf_download_sample_with_subtitle_nohd.json", "https://utstreaming.zdf.de/mtt/zdf/17/02/170212_sendung2_sof/3/F1011017_hoh_deu_Lindstroem_Liebesreigen_in_Samlund_120217.xml", "https://rodlzdf-a.akamaihd.net/none/zdf/17/02/170212_sendung2_sof/3/170212_sendung2_sof_476k_p9v13.mp4", "https://rodlzdf-a.akamaihd.net/none/zdf/17/02/170212_sendung2_sof/3/170212_sendung2_sof_1496k_p13v13.mp4", "" },
            { "/zdf/zdf_download_sample_without_subtitle_hd.json", "", "https://nrodlzdf-a.akamaihd.net/de/zdf/17/02/170212_kompakt_spo/1/170212_kompakt_spo_229k_p7v13.mp4", "https://rodlzdf-a.akamaihd.net/de/zdf/17/02/170212_kompakt_spo/1/170212_kompakt_spo_1496k_p13v13.mp4", "https://rodlzdf-a.akamaihd.net/de/zdf/17/02/170212_kompakt_spo/1/170212_kompakt_spo_476k_p9v13.mp4" }
        });
    }
    
    private final String jsonFile;
    private final String subtitleUrl;
    private final String smallQualityUrl;
    private final String normalQualityUrl;
    private final String hdQualityUrl;

    public ZdfDownloadDTODeserializerTest(String jsonFile, String subtitleUrl, String smallQualityUrl, String normalQualityUrl, String hdQualityUrl) {
        this.jsonFile = jsonFile;
        this.subtitleUrl = subtitleUrl;
        this.smallQualityUrl = smallQualityUrl;
        this.normalQualityUrl= normalQualityUrl;
        this.hdQualityUrl = hdQualityUrl;
    }
    
    @Test
    public void testDeserialize() {
        
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        ZDFDownloadDTODeserializer target = new ZDFDownloadDTODeserializer();
        DownloadDTO actual = target.deserialize(jsonObject, DownloadDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getSubTitleUrl(), equalTo(subtitleUrl));
        assertThat(actual.getUrl(Qualities.SMALL), equalTo(smallQualityUrl));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(normalQualityUrl));
        assertThat(actual.getUrl(Qualities.HD), equalTo(hdQualityUrl));
    }
}
