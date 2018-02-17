package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
public class WdrFilmPartDeserializerTest {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {  
      { 
        "/wdr/wdr_film_aktuell.html",
        "WDR aktuell",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-deniz-yuecel-ist-frei-102.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-deutschlandtrend-und-jusos-no-groko-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-sicherheitskonferenz-in-muenchen-102.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-degowski-aus-haft-entlassen-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-mutmasslicher-serienvergewaltiger-stellt-sich-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-kompakt-21112.html",
          "https://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-wetter-19788.html"
        }
      },
      {
        "/wdr/wdr_film_lokalzeit.html",
        "Lokalzeit",
        new String[] {
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-der-zug-in-aachen-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-grenzenlos-jeck---wenn-briten-feiern-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-rosenmontag-im-schnee-monschau-hoefen-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-kompakt-20998.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-der-prinz-im-rollstuhl-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-kinderzug-aachen---mit-hogwarts-schule-unterwegs-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-der-lichterzug-in-eiserfey-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-kehraus-nach-dem-zug-100.html",
          "https://www1.wdr.de/mediathek/video/sendungen/lokalzeit-aachen/video-wetter-19670.html"          
        }
      },
      {
        "/wdr/wdr_film1.html",
        "Abenteuer Erde",
        new String[0]
      }
    });
  }

  private final String filmPageFile;
  private final String topic;
  private final TopicUrlDTO[] expectedUrls;
  
  public WdrFilmPartDeserializerTest(final String aFilmPageFilme,
    final String aTopic,
    final String[] aExpectedUrls
  ) {
    filmPageFile = aFilmPageFilme;
    topic = aTopic;
    expectedUrls = new TopicUrlDTO[aExpectedUrls.length];
    
    for (int i = 0; i < aExpectedUrls.length; i++) {
      expectedUrls[i] = new TopicUrlDTO(topic, aExpectedUrls[i]);
    }
  }
  
  @Test
  public void deserializeTest() {
    final Document document = Jsoup.parse(FileReader.readFile(filmPageFile));
    
    WdrFilmPartDeserializer target = new WdrFilmPartDeserializer();
    
    Set<TopicUrlDTO> actual = target.deserialize(topic, document);
    
    assertThat(actual.size(), equalTo(expectedUrls.length));
    
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
