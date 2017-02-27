package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.JsonFileReader;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ZdfEntryDTODeserializerParameterizedTest {
  
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {   
            { "/zdf/zdf_search_page_entry_sample1.json", "https://api.zdf.de/content/documents/trailer-chefsache-100.json", "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/170224_sendung_tr_sok4" },
            { "/zdf/zdf_search_page_entry_sample_fullurls.json", "https://api.zdf.de/content/documents/trailer-chefsache-100.json", "https://api.zdf.de/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/170224_sendung_tr_sok4" },
        });
    }    
    
    private final String jsonFile;
    private final String generalUrl;
    private final String downloadUrl;
    
    public ZdfEntryDTODeserializerParameterizedTest(String jsonFile, String generalUrl, String downloadUrl) {
        this.jsonFile = jsonFile;
        this.generalUrl = generalUrl;
        this.downloadUrl = downloadUrl;
    }
    
    @Test
    public void testDeserialize() throws IOException, URISyntaxException {
        JsonObject jsonObject = JsonFileReader.readJson(jsonFile);
        
        ZDFEntryDTODeserializer target = new ZDFEntryDTODeserializer();
        ZDFEntryDTO actual = target.deserialize(jsonObject, ZDFEntryDTO.class, null);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getEntryGeneralInformationUrl(), equalTo(generalUrl));
        assertThat(actual.getEntryDownloadInformationUrl(), equalTo(downloadUrl));
    }
}
