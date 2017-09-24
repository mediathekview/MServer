package mServer.crawler.sender.hr;

import java.util.List;
import mServer.test.HtmlFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import static org.junit.Assert.*;

public class HrSendungOverviewDeserializerTest {
    
    @Test
    public void deserializeTest() {
        String html = HtmlFileReader.readHtmlPage("/hr/hr_sendung_overview1.html");
        Document document = Jsoup.parse(html);
        
        HrSendungOverviewDeserializer target = new HrSendungOverviewDeserializer();
        List<String> actual = target.deserialize(document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.size(), equalTo(3));
        assertThat(actual, Matchers.containsInAnyOrder("http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen-chiemsee,sendung-12870.html", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-12502.html"));
    }
}
