package mServer.test;

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
    
    public void setUp(String url, String jsonFile) {
        when(mock.execute(url)).thenReturn(JsonFileReader.readJson(jsonFile));
    }
}
