package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ZdfFilmDetailTaskMultipleLanguagesTest extends ZdfTaskTestBase {

  @Test
  public void testGermanAndEnglish() {

    final String[] expectedTitles =
        new String[] {
          "Hard Sun (1)", "Hard Sun (1) (Englisch)",
        };
    final String[] expectedUrlsSmall =
        new String[] {
          getWireMockBaseUrlSafe()
              + "/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_476k_p9v13.mp4",
          getWireMockBaseUrlSafe()
              + "/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_476k_p9v13.mp4",
        };
    final String[] expectedUrlsNormal =
        new String[] {
          getWireMockBaseUrlSafe()
              + "/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_1496k_p13v13.mp4",
          getWireMockBaseUrlSafe()
              + "/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_1496k_p13v13.mp4",
        };
    final String[] expectedSubitleUrls =
            new String[] {
                    "https://utstreaming.zdf.de/mtt/zdf/18/04/180416_2215_sendung_hsn/7/Hard_Sun_Teil1_OmU.xml",
                    "",
            };

    final String filmUrl = "/content/documents/zdf/serien/hardsun/hard-sun-1-100.json";
    final String videoUrl = "/tmd/2/android_native_5/vod/ptmd/mediathek/180416_2215_sendung_hsn";
    setupSuccessfulJsonResponse(filmUrl, "/zdf/zdf_film_details_english.json");
    setupSuccessfulJsonResponse(videoUrl, "/zdf/zdf_video_details_english.json");

    setupHeadResponse(404);

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(2));

    final SortedSet<Film> sortedActual = new TreeSet<>(Comparator.comparing(Film::getTitel));
    sortedActual.addAll(actual);

    for (int i = 0; i < sortedActual.size(); i++) {
      AssertFilm.assertEquals(
          sortedActual.toArray(new Film[] {})[i],
          Sender.ZDF,
          "Hard Sun",
          expectedTitles[i],
          LocalDateTime.of(2018, 4, 18, 0, 40, 0),
          Duration.ofMinutes(102).plusSeconds(53),
          "Die beiden Londoner Polizisten Charlie Hicks und Elaine Renko gelangen an geheime Dokumente und werden vom MI5 gejagt. Denn die Dateien haben einen äußerst brisanten Inhalt.",
          "https://www.zdf.de/serien/hardsun/hard-sun-1-100.html",
          new GeoLocations[] {GeoLocations.GEO_DE},
          expectedUrlsSmall[i],
          expectedUrlsNormal[i],
          "",
          expectedSubitleUrls[i]);
    }
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + aDetailUrl));
    return new ZdfFilmDetailTask(createCrawler(), getWireMockBaseUrlSafe(), urls, null, createPartnerMap()).invoke();
  }
  
  private Map<String, Sender> createPartnerMap() {
    Map<String, Sender> partnerMap = new HashMap<>();
    partnerMap.put("ZDFinfo", Sender.ZDF);
    partnerMap.put("ZDFneo", Sender.ZDF);
    partnerMap.put("ZDF", Sender.ZDF); 
    partnerMap.put("EMPTY", Sender.ZDF);
      // IGNORED Sender [KI.KA, WDR, PHOENIX, one, HR, 3sat, SWR, arte, BR, RBB, ARD, daserste, alpha, MDR, radiobremen, funk, ZDF, NDR, SR]
    return partnerMap;
  }
}
