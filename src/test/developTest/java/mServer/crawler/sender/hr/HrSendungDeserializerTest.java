package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.test.HtmlFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class HrSendungDeserializerTest {
    
    @Test
    public void deserializeTest() {
        DatenFilm expected = new DatenFilm(
                Const.HR, 
                "service: reisen", 
                "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html",
                "Von Marienbad nach Karlsbad",
                "http://www.hr.gl-systemhaus.de/video/as/servicereisen/2017_09/hrLogo_170919120157_0193742_512x288-25p-500kbit.mp4",
                "",
                "19.09.17",
                "18:50:00",
                1506,
                "\"service: reisen\" begibt sich auf eine ganz besondere Trinkkur durch Böhmen und entdeckt dabei nicht nur bemerkenswerte Heilquellen, sondern auch modernere Therapie-Formen wie das Bierwellness."
        );
        
        String html = HtmlFileReader.readHtmlPage("/hr/hr_sendung_detail1.html");
        Document document = Jsoup.parse(html);
        
        HrSendungDeserializer target = new HrSendungDeserializer();
        DatenFilm actual = target.deserialize("service: reisen", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html", document);
        
        assertThat(actual, notNullValue());
        for(int i = 0; i < actual.arr.length; i++) {
            assertThat(actual.arr[i], equalTo(expected.arr[i]));
        }
    }
}
