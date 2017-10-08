package mServer.crawler.sender.wdr;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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
public class WdrDayPageDeserializerTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {  
            { "/wdr/wdr_sendung_day.html"
                , new String[][] 
                {
                    { "WDR.DOK", "http://www1.wdr.de/mediathek/video/sendungen/wdr-dok/video-die-kommissare-vom-rhein----jahre-koelner-tatort-100.html" },
                    { "Tatort", "http://www1.wdr.de/mediathek/video/sendungen/tatort/video-nachbarn-100.html" },
                    { "Lokalzeitgeschichten", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeitgeschichten/video-lokalzeit-geschichten---heimat-100.html" },
                    { "Aktuelle Stunde", "http://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2150.html" },
                    { "Taminas ReiseTest", "http://www1.wdr.de/mediathek/video/sendungen/video-taminas-reisetest-radtouren--kurztrip-auf-zwei-raedern-104.html" },
                    { "Münsterland Giro 2017", "http://www1.wdr.de/mediathek/video/sendungen/video-muensterland-giro--104.html" },
                    { "Unterhaltung", "http://www1.wdr.de/mediathek/video/sendungen/unterhaltung/video-ein-herz-und-eine-seele---besuch-aus-der-ostzone-100.html" },
                    { "Servicezeit", "http://www1.wdr.de/mediathek/video/sendungen/servicezeit/video-bjoern-freitags-streetfood-duell-pulled-korean-bbq-taco-100.html" },
                    { "Lecker an Bord", "http://www1.wdr.de/mediathek/video/sendungen/lecker-an-bord/video-kaffee-bier-und-historisches-brot--100.html" },
                    { "Flussgeschichten", "http://www1.wdr.de/mediathek/video/sendungen/video-flussgeschichten---die-ruhr-104.html" },
                    { "Fernsehfilm", "http://www1.wdr.de/mediathek/video/sendungen/fernsehfilm/video-ein-hausboot-zum-verlieben-100.html" },
                    { "Fernsehfilm", "http://www1.wdr.de/mediathek/video/sendungen/fernsehfilm/video-die-farben-der-liebe-102.html" },
                }
            }
        });
    }

    private final String htmlFile;
    private final String[][] expectedSendungen;
    
    private final WdrDayPageDeserializer target;
    
    public WdrDayPageDeserializerTest(String aHtmlFile, String[][] aSendungen) {
        htmlFile = aHtmlFile;
        expectedSendungen = aSendungen;
        
        target = new WdrDayPageDeserializer();
    }
    
    @Test
    public void deserializeTest() {
        
        String html = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(html);
        
        List<WdrSendungDto> actual = target.deserialize(document);

        assertThat(actual, notNullValue());  
        assertThat(actual.size(), equalTo(expectedSendungen.length));
        for(int i = 0; i < actual.size(); i++) {
            WdrSendungDto actualDto = actual.get(i);
            assertThat(actualDto.getTheme(), equalTo(expectedSendungen[i][0]));
            assertThat(actualDto.getVideoUrls().size(), equalTo(1));
            assertThat(actualDto.getVideoUrls().get(0), equalTo(expectedSendungen[i][1]));
        }
    }
}
