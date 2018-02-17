package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class WdrTopicOverviewDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      {
        "/wdr/wdr_topic_overview.html",
        "Ausgerechnet",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-108.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---camping-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kaffee-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---kreuzfahrt-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-pizza-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kueche-was-kosten-herd--co-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet-kaese--100.html"
        },
        new String[0]
      },
      {
        "/wdr/wdr_topic_lokalzeit1.html",
        "Lokalzeit Ruhr",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-litruhr-autoren-lernen-revier-kennen-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-flamingo-horst-kevin-und-seine-geschwister-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-stromkasten-happening-in-gladbeck-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-neuer-wanderweg-baldeneysteig-wird-eroeffnet-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-tomatenzucht-auf-zechenbrachen-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-brezelfest-in-bottrop-kirchhellen--100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-ameisen-als-haustiere--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-opel---das-ende-mit-schrecken-100.html"          
        },
        new String[0]
      },
      {
        "/wdr/wdr_topic_lokalzeit2.html",
        "Lokalzeit Ruhr",
        new String[] { 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--134.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-am-samstag-240.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--132.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--130.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--128.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-ruhr--126.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit---tipps-und-tricks-fuer-den-sommer-und-herbstgarten-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-lokalzeit-radtour---zwei-reporter-unterwegs--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit/video-rammes-gartenzeit-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---ergebnisse-aus-den-wahlkreisen-106.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-landtagswahl---der-wahltag-im-ruhrgebiet-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-litruhr-autoren-lernen-revier-kennen-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-flamingo-horst-kevin-und-seine-geschwister-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-stromkasten-happening-in-gladbeck-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-spektakulaere-lieferung-fuer-essener-rathaus-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-liebeskonferenz-in-essen-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-der-gassi-geh-papagei-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-das-tollste-tier-im-revier-super-duck-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-gruen-riesengarten-in-bochum-102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video--gruen-mit-dem-rad-durchs-revier-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-service-aerger-mit-dem-stromanbieter-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-lokalzeit-bitte-kommen-schaden-am-autodach---keiner-wills-gewesen-sein-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-tobias-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/video-hans-im-glueck-glueck-im-bogenschiessen-100.html"          
        },
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-beitraege-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-das-tollste-tier100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-bitte-kommen-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-hans-im-glueck100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-ruhr/lokalzeit-ruhr-reihe-dreihundertsechzig-grad-gruen-100.html" 
        }        
      }
    });
  }
  
  private final String htmlFile;
  private final String topic;
  private final WdrTopicUrlDTO[] expectedUrls;
  
  public WdrTopicOverviewDeserializerTest(final String aHtmlFile,
    final String aTopic,
    final String[] aExpectedFilmUrls,
    final String[] aExpectedOverviewUrls
  ) {
    htmlFile = aHtmlFile;
    topic = aTopic;
    
    expectedUrls = new WdrTopicUrlDTO[aExpectedFilmUrls.length + aExpectedOverviewUrls.length];
    for (int i = 0; i < aExpectedFilmUrls.length; i++) {
       expectedUrls[i] = new WdrTopicUrlDTO(aTopic, aExpectedFilmUrls[i], true);
    }

    for (int i = 0; i < aExpectedOverviewUrls.length; i++) {
       expectedUrls[aExpectedFilmUrls.length + i] = new WdrTopicUrlDTO(aTopic, aExpectedOverviewUrls[i], false);
    }
  }
  
  @Test
  public void deserializeTest() {
    final String htmlContent = FileReader.readFile(htmlFile);
    final Document document = Jsoup.parse(htmlContent);
    
    WdrTopicOverviewDeserializer target = new WdrTopicOverviewDeserializer();
    
    List<WdrTopicUrlDTO> actual = target.deserialize(topic, document);
    
    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
