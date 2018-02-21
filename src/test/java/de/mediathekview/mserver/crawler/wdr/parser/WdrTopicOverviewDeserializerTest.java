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
      },
      {
        "/wdr/wdr_topic_overview_rockpalast_first_year.html",
        "Rockpalast",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-korn---summer-breeze--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-kreator---summer-breeze--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-heaven-shall-burn---summer-breeze--102.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-with-full-force--mit-ministry-combichrist-adept-callejon-und-rykers-100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-umse---summerjam--100.html"
        },
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/index.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/rockpalast-108.html"
        }
      },
      {
        "/wdr/wdr_topic_overview_rockpalast_second_year.html",
        "Rockpalast",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-alter-bridge---koeln-palladium--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-like-a-storm---koeln-palladium--100.html", 
          "https://www1.wdr.de/mediathek/video/sendungen/rockpalast/video-rockpalast-backstage-drangsal-100.html"
        },
        new String[0]
      },
      {
        "/wdr/wdr4_topic1.html",
        "WDR 4 Events",
        new String[] {
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-wdr--singt-mit-guildo-der-mitsing-spass-mit-guildo-horn-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-querbeat-mit-barbarossaplatz-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-paveier-mit-dat-is-freiheit-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-blaeck-foeoess-mit-lommer-nimmie-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-das-scalaensemble-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-die-micky-bruehl-band-mit-immer-singe-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-die-raeuber-mit-fuer-die-iwigkeit-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-die-hoehner-mit-wir-sind-fuer-die-liebe-gemacht-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-cat-ballou-mit-et-jitt-kein-wood-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-immer-wieder-neue-lieder-102.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-xylophonmedley-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-kasalla-mit-mer-sin-eins-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-die-wdr--band-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-albert-hammond-beim-wdr--sommer-open-air-mit-one-moment-in-time-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-omd-beim-wdr--sommer-open-air-mit-forever-live-and-die-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-omd-beim-wdr--sommer-open-air-mit-walking-on-the-milky-way-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-omd-beim-wdr--sommer-open-air-mit-sailing-on-the-seven-seas-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-albert-hammond-beim-wdr--sommer-open-air-mit-free-electric-band-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-albert-hammond-beim-wdr--sommer-open-air-mit-it-never-rains-in-southern-california-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-omd-beim-wdr--sommer-open-air-mit-maid-of-orleans-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-best-of-wdr--singt-mit-guildo-horn-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-wdr--radiokonzert-mit-marillion-102.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-wdr--singt-mit-guildo-horn---delilah-von-tom-jones-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-wdr--singt-mit-guildo-horn---diana-von-paul-anka-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-live-bei-wdr--johnny-logan-singt-hold-me-now-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-live-bei-wdr--johnny-logan-singt-it-is-what-it-is-100.html"
        },
        new String[0]
      },
      {
        "/wdr/wdr4_topic2.html",
        "Ullas Lieblingsrezepten",
        new String[] {
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-rote-bete-ricotta-bratling-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-matjes-hausfrauenart-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-curry-von-roten-linsen-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-wirsing-kartoffel-untereinander-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-senfcremesuppe-mit-sahnehaeubchen-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-gruenkohleintopf-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-birnengratin-mit-gorgonzola-100.html",
          "https://www1.wdr.de/mediathek/av/video-gratinierter-chicoree-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-weihnachtsmenue-hauptspeise--ochsenbaeckchen-in-burgunder-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-weihnachtsmenue-vorspeise--gratinierter-ziegenkaese-auf-feldsalat-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-schokoladentarte-mit-lebkuchengewuerz-100.html",
          "https://www1.wdr.de/mediathek/av/video-brutti-ma-buoni---italienische-nuss-baisers-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-ullas-lieblingsrezepte-lauch-kartoffelstampf-mit-essig-und-speck-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-ullas-lieblingsrezepte-kuerbis-lasagne-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-reibekuchen-mit-apfelkompott-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-zwiebelstrudel-auf-sauerrahm-senfsauce-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-bratkartoffeln-mit-schnittlauchquark-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-apfelkuchen-mit-knuspermandeln-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-lauchknoedel-auf-pfifferlingsauce-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-schnitzel-in-der-kokoskruste-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-crema-catalana-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-gebeizter-lachs-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-mangold-frittata-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-rote-bete-pesto-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-mediterraner-bohnensalat-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-gruene-zwiebelsuppe-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-champignon-paste-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-erdbeertoertchen-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-huehnerfrikassee-mit-spargel-100.html",
          "https://www1.wdr.de/mediathek/video/radio/wdr4/video-herzoginkartoffeln-oder-pommes-duchesse-100.html"
        },
        new String[0]
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
