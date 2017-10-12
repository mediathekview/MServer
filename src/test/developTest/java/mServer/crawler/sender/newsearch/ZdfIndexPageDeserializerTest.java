package mServer.crawler.sender.newsearch;

import java.util.Arrays;
import java.util.Collection;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ZdfIndexPageDeserializerTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {  
            { "/zdf/zdf_index_page_with_bearer.html", "309fa9bc88933de7256f4f6f6c5d3373cc36517c", "69c4eddbe0cf82b2a9277e8106a711db314a3008" },
            { "/zdf/zdf_index_page_without_bearer.html", "", "" }
        });
    }

    private final String htmlFile;
    private final String expectedBearerSearch;
    private final String expectedBearerVideo;
    
    private final ZdfIndexPageDeserializer target;
    
    public ZdfIndexPageDeserializerTest(String aHtmlFile, String aExpectedBearerSearch, String aExpectedBearerVideo) {
        htmlFile = aHtmlFile;
        expectedBearerSearch = aExpectedBearerSearch;
        expectedBearerVideo = aExpectedBearerVideo;
        
        target = new ZdfIndexPageDeserializer();
    }
    
    @Test
    public void deserializeTestIndexPageWithBearer() {
        String htmlPage = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(htmlPage);
        
        ZDFConfigurationDTO actual = target.deserialize(document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getApiToken(ZDFClient.ZDFClientMode.SEARCH), equalTo(expectedBearerSearch));
        assertThat(actual.getApiToken(ZDFClient.ZDFClientMode.VIDEO), equalTo(expectedBearerVideo));
    }
}
