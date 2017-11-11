package mServer.crawler.sender.hr;

import java.util.List;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import static org.junit.Assert.*;

public class HrSendungenListDeserializerTest {
    
    @Test
    public void deserializeTest() {
        String html = TestFileReader.readFile("/hr/hr_sendungen.html");
        Document document = Jsoup.parse(html);
        
        HrSendungenListDeserializer target = new HrSendungenListDeserializer();
        List<HrSendungenDto> actual = target.deserialize(document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.size(), equalTo(29));
        assertThat(actual.get(0).getTheme(), equalTo("alle wetter!"));
        assertThat(actual.get(0).getUrl(), equalTo("http://www.hr-fernsehen.de/sendungen-a-z/alle-wetter/sendungen/index.html"));
    }
}
