package mServer.crawler.sender.wdr;

import java.util.Arrays;
import java.util.Collection;
import mServer.crawler.CrawlerConfig;
import mServer.test.TestFileReader;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
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
            { CrawlerConfig.LOAD_SHORT, "/wdr/wdr_sendung_overview.html", new String[] {}, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-108.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---camping-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kaffee-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kreuzfahrt-102.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-100.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kueche-was-kosten-herd--co-100.html", "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--100.html" } },
            { CrawlerConfig.LOAD_SHORT, "/wdr/wdr_sendung_overview_lokalzeit.html", new String[] { "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-beitraege-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-das-tollste-tier100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-bitte-kommen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-hans-im-glueck100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-dreihundertsechzig-grad-gruen-100.html" }, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--134.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-am-samstag-240.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--132.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--130.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--128.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--126.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit---tipps-und-tricks-fuer-den-sommer-und-herbstgarten-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-radtour---zwei-reporter-unterwegs--100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---ergebnisse-aus-den-wahlkreisen-106.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---der-wahltag-im-ruhrgebiet-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-litruhr-autoren-lernen-revier-kennen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-flamingo-horst-kevin-und-seine-geschwister-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-stromkasten-happening-in-gladbeck-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-spektakulaere-lieferung-fuer-essener-rathaus-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-liebeskonferenz-in-essen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-der-gassi-geh-papagei-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-super-duck-102.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-gruen-riesengarten-in-bochum-102.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video--gruen-mit-dem-rad-durchs-revier-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-service-aerger-mit-dem-stromanbieter-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-schaden-am-autodach---keiner-wills-gewesen-sein-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-tobias-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-glueck-im-bogenschiessen-100.html" } },
            { CrawlerConfig.LOAD_SHORT, "/wdr/wdr_sendung_overview_lokalzeit_mehr.html", new String[] {}, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-litruhr-autoren-lernen-revier-kennen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-flamingo-horst-kevin-und-seine-geschwister-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-stromkasten-happening-in-gladbeck-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-neuer-wanderweg-baldeneysteig-wird-eroeffnet-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-tomatenzucht-auf-zechenbrachen-100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-brezelfest-in-bottrop-kirchhellen--100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-ameisen-als-haustiere--100.html", "http://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-opel---das-ende-mit-schrecken-100.html" } },
            { CrawlerConfig.LOAD_SHORT, "/wdr/wdr_sendung_overview_rockpalast_first_year.html", new String[] {}, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-korn---summer-breeze--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-kreator---summer-breeze--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-heaven-shall-burn---summer-breeze--102.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-with-full-force--mit-ministry-combichrist-adept-callejon-und-rykers-100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-umse---summerjam--100.html" } },
            { CrawlerConfig.LOAD_LONG, "/wdr/wdr_sendung_overview_rockpalast_first_year.html", new String[] { "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/index.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/rockpalast-108.html" }, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-korn---summer-breeze--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-kreator---summer-breeze--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-heaven-shall-burn---summer-breeze--102.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-with-full-force--mit-ministry-combichrist-adept-callejon-und-rykers-100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-umse---summerjam--100.html" } },
            { CrawlerConfig.LOAD_SHORT, "/wdr/wdr_sendung_overview_rockpalast_second_year.html", new String[] {}, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-alter-bridge---koeln-palladium--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-like-a-storm---koeln-palladium--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-backstage-drangsal-100.html" } },
            { CrawlerConfig.LOAD_LONG, "/wdr/wdr_sendung_overview_rockpalast_second_year.html", new String[] {}, new String[] { "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-alter-bridge---koeln-palladium--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-like-a-storm---koeln-palladium--100.html", "http://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-backstage-drangsal-100.html" } }
        });
    }
                
    private final int crawlerLength;
    private final String htmlFile;
    private final String[] expectedOverviewUrls;
    private final String[] expectedVideoUrls;
    
    private final WdrSendungOverviewDeserializer target;
    
    public WdrSendungOverviewDeserializerTest(int aCrawlerLength, String aHtmlFile, String[] aExpectedOverviewUrls, String[] aExpectedVideoUrls) {
        crawlerLength = aCrawlerLength;
        htmlFile = aHtmlFile;
        expectedOverviewUrls = aExpectedOverviewUrls;
        expectedVideoUrls = aExpectedVideoUrls;
        
        target = new WdrSendungOverviewDeserializer();
    }
    
    @Test
    public void deserializeTest() {
        String html = TestFileReader.readFile(htmlFile);
        Document document = Jsoup.parse(html);
        
        CrawlerConfig.senderLoadHow = crawlerLength;
        
        WdrSendungDto actual = target.deserialize(document);

        assertThat(actual, notNullValue());
        assertThat(actual.getTheme(), nullValue());
        assertThat(actual.getOverviewUrls(), Matchers.containsInAnyOrder(expectedOverviewUrls));
        assertThat(actual.getVideoUrls(), Matchers.containsInAnyOrder(expectedVideoUrls));
    }
}
