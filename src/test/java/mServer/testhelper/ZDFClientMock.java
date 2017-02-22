package mServer.testhelper;

import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URISyntaxException;
import mServer.crawler.sender.newsearch.ZDFClient;
import static org.mockito.Mockito.*;
import org.mockito.Mockito;

/**
 * Helper to mock the ZDFClient
 */
public class ZDFClientMock {
    
    private final ZDFClient mock;
    
    public ZDFClientMock() {
        mock = Mockito.mock(ZDFClient.class);
    }
    
    public ZDFClient get() {
        return mock;
    }
    
    public void setUp(String url, String jsonFile) throws IOException, URISyntaxException {
        WebResource mockWebResource = Mockito.mock(WebResource.class);
        when(mock.createResource(url)).thenReturn(mockWebResource);
        when(mock.execute(mockWebResource)).thenReturn(JsonFileReader.readJson(jsonFile));
    }
}
