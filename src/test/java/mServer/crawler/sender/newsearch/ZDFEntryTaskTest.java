package mServer.crawler.sender.newsearch;

import java.io.IOException;
import java.net.URISyntaxException;
import mSearch.Config;
import mServer.test.ZDFClientMock;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.junit.After;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZDFEntryTaskTest {

    @After
    public void cleanUp() {
        Config.setStop(false);
    }
    
    @Test
    public void testComputeReturnVideoDTO() throws IOException, URISyntaxException {
        
        String generalUrl = "https://api.zdf.de/generalUrl";
        String downloadUrl = "https://api.zdf.de/downloadUrl";

        ZDFClientMock mockZdfClient = new ZDFClientMock();
        mockZdfClient.setUp(generalUrl, "/zdf/zdf_entry_general_sample.json");
        mockZdfClient.setUp(downloadUrl, "/zdf/zdf_entry_download_sample.json");
        
        ZDFEntryDTO dto = new ZDFEntryDTO(generalUrl, downloadUrl);
        ZDFEntryTask target = new ZDFEntryTask(dto, mockZdfClient.get());        
        VideoDTO actual = target.invoke();
        
        assertThat(actual, notNullValue());
        assertThat(actual.getDownloadDto(), notNullValue());
    }

    @Test
    public void testComputeReturnsNullIfEntryDTOIsNull() {
        ZDFEntryTask target = new ZDFEntryTask(null);
        VideoDTO actual = target.invoke();
        
        assertThat(actual, nullValue());
    }

    @Test
    public void testComputeReturnsNullIfStopped() {
        Config.setStop(true);
        
        ZDFEntryDTO dto = new ZDFEntryDTO("", "");
        ZDFEntryTask target = new ZDFEntryTask(dto);
        VideoDTO actual = target.invoke();
        
        assertThat(actual, nullValue());
    }
}
