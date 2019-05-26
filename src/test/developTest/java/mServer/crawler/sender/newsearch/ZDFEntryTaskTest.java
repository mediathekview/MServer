package mServer.crawler.sender.newsearch;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;

import de.mediathekview.mlib.Config;
import mServer.test.ZDFClientMock;

public class ZDFEntryTaskTest {

    @After
    public void cleanUp() {
        Config.setStop(false);
    }
    
    @Test
    public void testComputeReturnVideoDTO() {
        
        String generalUrl = "https://api.zdf.de/generalUrl";
        String downloadUrl = "https://api.zdf.de/downloadUrl";

        ZDFClientMock mockZdfClient = new ZDFClientMock();
        mockZdfClient.setUp(generalUrl, "/zdf/zdf_entry_general_sample.json");
        mockZdfClient.setUp(downloadUrl, "/zdf/zdf_entry_download_sample.json");
        
        ZDFEntryDTO dto = new ZDFEntryDTO("https://api.zdf.de",generalUrl, downloadUrl,"zdf");
        ZDFEntryTask target = new ZDFEntryTask(dto, mockZdfClient.get());        
        VideoDTO actual = target.invoke();
        
        assertThat(actual, notNullValue());
        assertThat(actual.getDownloadDto(), notNullValue());
    }

    @Test
    public void testComputeReturnsNullIfEntryDTOIsNull() {
        ZDFEntryTask target = new ZDFEntryTask(null,"https://www.zdf.de","https://api.zdf.de","api.zdf.de",new ZDFConfigurationLoader("https://www.zdf.de").loadConfig());
        VideoDTO actual = target.invoke();
        
        assertThat(actual, nullValue());
    }

    @Test
    public void testComputeReturnsNullIfStopped() {
        Config.setStop(true);
        
        ZDFEntryDTO dto = new ZDFEntryDTO("https://api.zdf.de/","", "","");
        ZDFEntryTask target = new ZDFEntryTask(dto,"https://www.zdf.de","https://api.zdf.de","api.zdf.de",new ZDFConfigurationLoader("https://www.zdf.de").loadConfig());
        VideoDTO actual = target.invoke();
        
        assertThat(actual, nullValue());
    }
}
