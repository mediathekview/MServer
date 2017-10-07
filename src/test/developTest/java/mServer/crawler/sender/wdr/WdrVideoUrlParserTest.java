package mServer.crawler.sender.wdr;

import java.util.Arrays;
import java.util.Collection;
import mServer.crawler.sender.newsearch.Qualities;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrVideoUrlParserTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/wdr/wdr_video2.js", "http://sample.js", "/wdr/wdr_video2.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_0_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_4_av.m3u8", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml" },
            { "/wdr/wdr_video3.js", "http://sample_no_hd.js", "/wdr/wdr_video3.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5540994,528067_5540993,528067_5540992,528067_5540996,528067_5540995,.mp4.csmil/master.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5540994,528067_5540993,528067_5540992,528067_5540996,528067_5540995,.mp4.csmil/index_0_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/52/528067/,528067_5540994,528067_5540993,528067_5540992,528067_5540996,528067_5540995,.mp4.csmil/index_2_av.m3u8", "", "" }
        });
    }
    
    private final String jsUrl;
    private final String expectedUrlSmall;
    private final String expectedUrlNormal;
    private final String expectedUrlHd;
    private final String expectedUrlSubtitle;
    private final WdrVideoUrlParser target;    
    
    public WdrVideoUrlParserTest(String aJsFile, String aJsUrl, String aM3u8File, String aM3u8Url, String aUrlSmall, String aUrlNormal, String aUrlHd, String aUrlSubtitle) {
        jsUrl = aJsUrl;
        expectedUrlSmall = aUrlSmall;
        expectedUrlNormal = aUrlNormal;
        expectedUrlHd = aUrlHd;
        expectedUrlSubtitle = aUrlSubtitle;
        
        WdrUrlLoaderMock urlLoader = new WdrUrlLoaderMock();
        urlLoader.setUp(aJsUrl, aJsFile);
        urlLoader.setUp(aM3u8Url, aM3u8File);
        
        target = new WdrVideoUrlParser(urlLoader.get());
    }
    
    @Test
    public void parseTest() {
        
        WdrVideoDto actual = target.parse(jsUrl);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getUrl(Qualities.SMALL), equalTo(expectedUrlSmall));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(expectedUrlNormal));
        assertThat(actual.getUrl(Qualities.HD), equalTo(expectedUrlHd));
        assertThat(actual.getSubtitleUrl(), equalTo(expectedUrlSubtitle));
    }
}
