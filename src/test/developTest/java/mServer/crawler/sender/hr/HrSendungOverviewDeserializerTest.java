package mServer.crawler.sender.hr;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HrSendungOverviewDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/hr/hr_sendung_overview1.html", new String[] { "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen-chiemsee,sendung-12870.html", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-12502.html" } },
            { "/hr/hr_sendung_hessenschau_list.html", new String[] { "http://www.hessenschau.de/tv-sendung/video-43192.html", "http://www.hessenschau.de/tv-sendung/video-43030.html", "http://www.hessenschau.de/tv-sendung/video-42942.html", "http://www.hessenschau.de/tv-sendung/video-42800.html", "http://www.hessenschau.de/tv-sendung/video-42720.html", "http://www.hessenschau.de/tv-sendung/video-42662.html", "http://www.hessenschau.de/tv-sendung/video-42610.html", "http://www.hessenschau.de/tv-sendung/video-42526.html", "http://www.hr-online.de/website/archiv/hessenschau/hessenschau.jsp?t=20090101&type=v" } }
        });
    }

    private final String htmlFile;
    private final String[] expectedUrls;
    
    public HrSendungOverviewDeserializerTest(String aHtmlFile, String[] aUrls) {
        htmlFile = aHtmlFile;
        expectedUrls = aUrls;
    }
    
    @Test
    public void deserializeTest() {
        String html = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(html);
        
        HrSendungOverviewDeserializer target = new HrSendungOverviewDeserializer();
        List<String> actual = target.deserialize(document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.size(), equalTo(expectedUrls.length));
        assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
    }
}
