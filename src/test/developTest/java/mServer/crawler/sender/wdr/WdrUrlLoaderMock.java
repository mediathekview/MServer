package mServer.crawler.sender.wdr;

import mServer.test.TestFileReader;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;

public class WdrUrlLoaderMock {
    private final WdrUrlLoader mock;
    
    public WdrUrlLoaderMock() {
        mock = Mockito.mock(WdrUrlLoader.class);
    }
    
    public WdrUrlLoader get() {
        return mock;
    }
    
    public void setUp(String url, String file) {
        when(mock.executeRequest(url)).thenReturn(TestFileReader.readFile(file));
    }
}
