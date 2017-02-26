package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZDFClientIntegrationTest {
 
    @Test
    public void testSearchRequest() {
        ZDFClient target = new ZDFClient();
        
        JsonObject jsonData = target.executeSearch(1, 1, 0);
        assertThat(jsonData, notNullValue());
    }
    
    @Test
    public void testGeneralInformationRequest() {
    }
    
    @Test
    public void testDownloadRequest() {
        
    }
}
