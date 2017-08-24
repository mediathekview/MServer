package mServer.developTest.test;

import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import mServer.crawler.sender.zdf.ZDFClient;

/**
 * Helper to mock the ZDFClient
 */
public class ZDFClientMock
{

    private final ZDFClient mock;

    public ZDFClientMock()
    {
        mock = Mockito.mock(ZDFClient.class);
    }

    public ZDFClient get()
    {
        return mock;
    }

    public void setUp(final String url, final String jsonFile)
    {
        when(mock.execute(url)).thenReturn(JsonFileReader.readJson(jsonFile));
    }
}
