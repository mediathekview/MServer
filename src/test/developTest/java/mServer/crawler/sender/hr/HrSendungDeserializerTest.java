package mServer.crawler.sender.hr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HrSendungDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/hr/hr_sendung_detail1.html", "service: reisen", "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html",
                "http://www.hr-fernsehen.de/sendungen-a-z/service-reisen/sendungen/service-reisen,sendung-13268.html",
                "Von Marienbad nach Karlsbad",
                "http://www.hr.gl-systemhaus.de/video/as/servicereisen/2017_09/hrLogo_170919120157_0193742_512x288-25p-500kbit.mp4",
                "2017-09-19T18:50",
                Duration.ofSeconds(1506),
                "\"service: reisen\" begibt sich auf eine ganz besondere Trinkkur durch Böhmen und entdeckt dabei nicht nur bemerkenswerte Heilquellen, sondern auch modernere Therapie-Formen wie das Bierwellness."
            },
            { "/hr/hr_sendung_hessenschau_detail.html", "hessenschau", "http://www.hessenschau.de/tv-sendung/video-43192.html",
                "http://www.hessenschau.de/tv-sendung/video-43192.html",
                "hessenschau - ganze Sendung",
                "http://www.hr.gl-systemhaus.de/video/as/hessenschau/2017_09/hrLogo_170928200235_L273051_512x288-25p-500kbit.mp4",
                "2017-09-28T19:30",
                Duration.ofSeconds(1653),
                "Polizei hofft auf Hinweise zu totem Säugling / Ryanair lässt rund 18.000 Flüge ausfallen / Wahl-Nachlese zur AfD / Wem nutzt das Gesetz zur Wahlkreis-Neuordnung? / Zucker-Branche steht vor historischer Reform / Aids-Hilfe-Loveball in Frankfurt am Main / Hirschbrunft in der Alten Fasanerie Hanau"
            },
            { "/hr/hr_sendung_detail2.html", "hr-katzen", "http://www.hr-fernsehen.de/sendungen-a-z/hr-katzen/index.html",
                "http://www.hr-fernsehen.de/sendungen-a-z/hr-katzen/index.html",
                "Die neuen Pausenkatzen",
                "http://www.hr.gl-systemhaus.de/video/fs/allgemein/2017_07/170705102721_hr-Fernsehen_Hessischer_Rundfunk_hr-online.de.mp4",
                "2017-07-05T00:00",
                Duration.ofSeconds(145),
                "Sie sind Kult: die Pausenkatzen des Hessischen Rundfunks. Erfunden in den 70er Jahren klettern und tollen sie heute wieder in Randzeiten durchs Programm des hr-fernsehens. Ende 2015 hat der hr die Pausenkatzen neu produziert: flauschig und in ultrascharfer HD-Qualität."
             }
        });
    }
    
    private final String htmlFile;
    private final String theme;
    private final String url;

    private final String expectedTitle;
    private final String expectedVideoUrl;
    private final String expectedWebsite;
    private final String expectedDateTime;
    private final String expectedDescription;
    private final Duration expectedDuration;
    
    public HrSendungDeserializerTest(String aHtmlFile, String aTheme, String aUrl, String aWebsite, String aTitle, String aVideoUrl, String aDateTime, Duration aDuration, String aDescription) {
        htmlFile = aHtmlFile;
        theme = aTheme;
        url = aUrl;
        
        expectedDateTime = aDateTime;
        expectedDescription = aDescription;
        expectedDuration = aDuration;
        expectedTitle = aTitle;
        expectedVideoUrl = aVideoUrl;
        expectedWebsite = aWebsite;
    }
    
    @Test
    public void deserializeTestWithVideo() throws URISyntaxException {
        String html = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(html);
        
        HrSendungDeserializer target = new HrSendungDeserializer();
        Film actual = target.deserialize(theme, url, document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getSender(), equalTo(Sender.HR));
        assertThat(actual.getThema(), equalTo(theme));
        assertThat(actual.getTitel(), equalTo(expectedTitle));
        assertThat(actual.getBeschreibung(), equalTo(expectedDescription));
        assertThat(actual.getTime().toString(), equalTo(expectedDateTime));
        assertThat(actual.getWebsite(), equalTo(new URI(expectedWebsite)));
        assertThat(actual.getDuration(), equalTo(expectedDuration));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(new URI(expectedVideoUrl)));
    }
    
    @Test
    public void deserializeTestWithoutVideo() throws URISyntaxException {
        String html = TestFileReader.readFile("/hr/hr_sendung_detail_without_video.html");
        Document document = Jsoup.parse(html);
        
        HrSendungDeserializer target = new HrSendungDeserializer();
        Film actual = target.deserialize("heimspiel!", "http://www.hr-fernsehen.de/sendungen-a-z/heimspiel/sendungen/sobiech-und-stein-im-heimspiel,sendung-13662.html", document);
        
        assertThat(actual, nullValue());
    }
}
