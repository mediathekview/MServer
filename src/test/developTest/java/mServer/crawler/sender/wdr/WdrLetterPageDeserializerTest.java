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
public class WdrLetterPageDeserializerTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "/wdr/wdr_letter_page_empty.html", new String[][] {} },
            { "/wdr/wdr_letter_page_with.html"
                , new String[][] 
                { 
                    { "Abenteuer Erde", "http://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/abenteuer-erde-104.html", "" },
                    { "Aktuelle Stunde", "", "http://www1.wdr.de/mediathek/video/sendungen/aktuelle-stunde/video-aktuelle-stunde-2156.html" },
                    { "Annemie Hülchrath trifft Prominente!", "http://www1.wdr.de/mediathek/video/sendungen/comedy/annemie-kommt-uebersicht-100.html", "" },
                    { "Lokalzeit aus Aachen", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/index.html", "" },
                    { "Ausgerechnet", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/index.html", "" }
                } 
            }
        });
    }
    
    private final String htmlFile;
    private final String[][] expectedSendungen;
    
    private final WdrLetterPageDeserializer target;

    public WdrLetterPageDeserializerTest(String aHtmlFile, String[][] aSendungen) {
        htmlFile = aHtmlFile;
        expectedSendungen = aSendungen;
        
        target = new WdrLetterPageDeserializer();
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
            
            if(expectedSendungen[i][1].isEmpty()) {
                assertThat(actualDto.getOverviewUrls().size(), equalTo(0));
            } else {
                assertThat(actualDto.getOverviewUrls().size(), equalTo(1));
                assertThat(actualDto.getOverviewUrls().get(0), equalTo(expectedSendungen[i][1]));
            }
            
            if(expectedSendungen[i][2].isEmpty()) {
                assertThat(actualDto.getVideoUrls().size(), equalTo(0));
            } else {
                assertThat(actualDto.getVideoUrls().size(), equalTo(1));
                assertThat(actualDto.getVideoUrls().get(0), equalTo(expectedSendungen[i][2]));
            }
        }
    }
}
