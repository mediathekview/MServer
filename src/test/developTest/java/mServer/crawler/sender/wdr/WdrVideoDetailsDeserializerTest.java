package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.Const;
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
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrVideoDetailsDeserializerTest {
    
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {  
            { "/wdr/wdr_video_details1.html", "Abenteuer Erde", "Die Tricks des Überlebens 3) Im Wald", "Nur auf der Nordhalbkugel gibt es Wälder, deren Leben durch große Veränderungen geprägt wird. Jedes Jahr lässt sich hier ein wundersamer Wechsel beobachten: im Winter sinken die Temperaturen dramatisch und die Wälder werden völlig kahl. Im Frühjahr kehren mit steigenden Temperaturen die grünen Blätter und damit das Leben zurück. Autor/-in: Paul Bradshaw", "http://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/video-die-tricks-des-ueberlebens--im-wald-102.html", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_2_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_0_av.m3u8", "", "", "2017-09-26T20:15", Duration.ofSeconds(2600), "http://deviceids-medp.wdr.de/ondemand/148/1480611.js", "/wdr/wdr_video1.js", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8", "/wdr/wdr_video1.m3u8" },
            { "/wdr/wdr_video_details2.html", "Ausgerechnet", "Ausgerechnet - Schokolade", "Knapp 25 Prozent der Bevölkerung verzehren Schokolade mehrmals in der Woche. Hinzu kommt gut ein weiteres Viertel, das Schokolade etwa einmal pro Woche genießt. Frauen greifen hier generell häufiger zu als Männer.", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_0_av.m3u8", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_4_av.m3u8", "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml", "2017-07-15T16:00", Duration.ofSeconds(2615), "http://deviceids-medp.wdr.de/ondemand/140/1407842.js", "/wdr/wdr_video2.js", "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8", "/wdr/wdr_video2.m3u8" },
        });
    }

    private final String htmlFile;
    private final String expectedTheme;
    private final String expectedTitle;
    private final String expectedDescription;
    private final String expectedWebsite;
    private final String expectedVideoUrlSmall;
    private final String expectedVideoUrlNormal;
    private final String expectedVideoUrlHd;
    private final String expectedSubtitle;
    private final String expectedTime;
    private final Duration expectedDuration;
    
    private final WdrVideoDetailsDeserializer target;
    
    public WdrVideoDetailsDeserializerTest(String aHtmlFile, String aTheme, String aTitle, String aDescription, String aWebsite, String aVideoUrlNormal, String aVideoUrlSmall, String aVideoUrlHd, String aSubtitle, String aTime, Duration aDuration, String aJsUrl, String aJsFile, String aM3u8Url, String aM3u8File) {
        htmlFile = aHtmlFile;
        expectedDescription = aDescription;
        expectedDuration = aDuration;
        expectedTheme = aTheme;
        expectedTime = aTime;
        expectedTitle = aTitle;
        expectedSubtitle = aSubtitle;
        expectedVideoUrlSmall = aVideoUrlSmall;
        expectedVideoUrlNormal = aVideoUrlNormal;
        expectedVideoUrlHd = aVideoUrlHd;
        expectedWebsite = aWebsite;
        
        WdrUrlLoaderMock urlLoader = new WdrUrlLoaderMock();
        urlLoader.setUp(aJsUrl, aJsFile);
        urlLoader.setUp(aM3u8Url, aM3u8File);
        
        target = new WdrVideoDetailsDeserializer(urlLoader.get());
    }
    
    @Test
    public void deserializeTestWithVideo() throws URISyntaxException {
        String html = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(html);
        
        Film actual = target.deserialize(expectedTheme, document);
        
        assertThat(actual, notNullValue());
        assertThat(actual.getSender(), equalTo(Sender.WDR));
        assertThat(actual.getThema(), equalTo(expectedTheme));
        assertThat(actual.getTitel(), equalTo(expectedTitle));
        assertThat(actual.getBeschreibung(), equalTo(expectedDescription));
        assertThat(actual.getWebsite(), equalTo(new URI(expectedWebsite)));
        assertThat(actual.getTime().toString(), equalTo(expectedTime));
        assertThat(actual.getDuration(), equalTo(expectedDuration));
        assertThat(actual.getUrl(Qualities.NORMAL), equalTo(new URI(expectedVideoUrlNormal)));
        assertThat(actual.getUrl(Qualities.SMALL), equalTo(new URI(expectedVideoUrlSmall)));
        
        if(expectedVideoUrlHd.isEmpty()) {
            assertThat(actual.hasHD(), equalTo(false));
        } else {
            assertThat(actual.getUrl(Qualities.HD), equalTo(new URI(expectedVideoUrlHd)));
        }
        
        if(expectedSubtitle.isEmpty()) {
            assertThat(actual.getSubtitles().isEmpty(), equalTo(true));
        } else {
            assertThat(actual.getSubtitles(), Matchers.containsInAnyOrder(new URI(expectedSubtitle)));
        }
    }
}
