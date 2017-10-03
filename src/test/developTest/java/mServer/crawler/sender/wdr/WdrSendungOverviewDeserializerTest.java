package mServer.crawler.sender.wdr;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import mServer.test.HtmlFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrSendungOverviewDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {  
            { "/wdr/wdr_sendung_overview.html", "Ausgerechnet", new String[] { "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-108.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---camping-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kaffee-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kreuzfahrt-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-100.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kueche-was-kosten-herd--co-100.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--100.html" } },
            { "/wdr/wdr_sendung_overview_lokalzeit.html", "Lokalzeit Ruhr", new String[] { "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--134.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-am-samstag-240.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--132.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--130.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--128.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--126.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit---tipps-und-tricks-fuer-den-sommer-und-herbstgarten-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-radtour---zwei-reporter-unterwegs--100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---ergebnisse-aus-den-wahlkreisen-106.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---der-wahltag-im-ruhrgebiet-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-litruhr-autoren-lernen-revier-kennen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-flamingo-horst-kevin-und-seine-geschwister-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-stromkasten-happening-in-gladbeck-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-spektakulaere-lieferung-fuer-essener-rathaus-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-liebeskonferenz-in-essen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-beitraege-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-der-gassi-geh-papagei-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-super-duck-102.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-das-tollste-tier100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-gruen-riesengarten-in-bochum-102.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video--gruen-mit-dem-rad-durchs-revier-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-dreihundertsechzig-grad-gruen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-service-aerger-mit-dem-stromanbieter-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-schaden-am-autodach---keiner-wills-gewesen-sein-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-bitte-kommen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-tobias-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-glueck-im-bogenschiessen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-hans-im-glueck100.html"} }
        });
    }
                
    private final String htmlFile;
    private final String expectedTheme;
    private final String[] expectedUrls;
    
    private final WdrSendungOverviewDeserializer target;
    
    public WdrSendungOverviewDeserializerTest(String aHtmlFile, String aTheme, String[] aExpectedUrls) {
        htmlFile = aHtmlFile;
        expectedTheme = aTheme;
        expectedUrls = aExpectedUrls;
        
        target = new WdrSendungOverviewDeserializer();
    }
    
    @Test
    public void deserializeTest() {
        String html = HtmlFileReader.readHtmlPage(htmlFile);
        Document document = Jsoup.parse(html);
        
        WdrSendungOverviewDto actual = target.deserialize("http://www1.wdr.de", document);

        assertThat(actual, notNullValue());
        assertThat(actual.getTheme(), equalTo(expectedTheme));
        assertThat(actual.getUrls(), Matchers.containsInAnyOrder(expectedUrls));
    }
}
