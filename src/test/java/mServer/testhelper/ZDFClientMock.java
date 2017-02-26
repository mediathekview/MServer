package mServer.testhelper;

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
        when(mock.execute(url)).thenReturn(JsonFileReader.readJson(jsonFile));
    }
}
