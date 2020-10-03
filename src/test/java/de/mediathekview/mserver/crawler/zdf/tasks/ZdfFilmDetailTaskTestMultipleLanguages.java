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

public class ZdfFilmDetailTaskTestMultipleLanguages extends ZdfTaskTestBase {

  @Test
  public void testGermanAndEnglish() {

    final String[] expectedTitles =
        new String[] {
          "Hard Sun (1)", "Hard Sun (1) (Englisch)",
        };
    final String[] expectedUrlsSmall =
        new String[] {
          "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_476k_p9v13.mp4",
          "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_476k_p9v13.mp4",
        };
    final String[] expectedUrlsNormal =
        new String[] {
          "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a1a2_1496k_p13v13.mp4",
          "http://localhost:8589/de/zdf/18/04/180416_2215_sendung_hsn/7/180416_2215_sendung_hsn_a3a4_1496k_p13v13.mp4",
        };

    final String filmUrl = "/content/documents/zdf/serien/hardsun/hard-sun-1-100.json";
    final String videoUrl = "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/180416_2215_sendung_hsn";
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
          "https://utstreaming.zdf.de/mtt/zdf/18/04/180416_2215_sendung_hsn/7/Hard_Sun_Teil1_OmU.xml");
    }
  }

  @Test
  public void testGermanWithAudiodescription() {

    final String[] expectedTitles =
        new String[] {
          "Schatz, nimm du sie!", "Schatz, nimm du sie! (Audiodeskription)",
        };
    final String[] expectedUrlsSmall =
        new String[] {
          "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_776k_p11v14.mp4",
          "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_776k_p11v14.mp4",
        };
    final String[] expectedUrlsNormal =
        new String[] {
          "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_1496k_p13v14.mp4",
          "http://localhost:8589/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_1496k_p13v14.mp4",
        };

    final String filmUrl = "/content/documents/zdf/filme/montagskino/schatz-nimm-du-sie-100.json";
    final String videoUrl = "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/190715_schatz_nimm_du_sie_mok";
    setupSuccessfulJsonResponse(filmUrl, "/zdf/zdf_film_details_with_audiodescription.json");
    setupSuccessfulJsonResponse(videoUrl, "/zdf/zdf_video_details_with_audiodescription.json");

    setupHeadResponse(404);

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(2));

    final SortedSet<Film> sortedActual = new TreeSet<>(Comparator.comparing(Film::getTitel));
    sortedActual.addAll(actual);

    for (int i = 0; i < sortedActual.size(); i++) {
      AssertFilm.assertEquals(
          sortedActual.toArray(new Film[] {})[i],
          Sender.ZDF,
          "Montagskino",
          expectedTitles[i],
          LocalDateTime.of(2019, 7, 15, 22, 15, 0),
          Duration.ofMinutes(89).plusSeconds(1),
          "Ein Elternpaar will sich trennen: Routine im Zeitalter der Patchworkfamilie. Doch keiner will die Kinder, weil sie kurzfristigen Karriereplänen im Wege stehen. (Film: FSK 12)",
          "https://www.zdf.de/filme/montagskino/schatz-nimm-du-sie-100.html",
          new GeoLocations[] {GeoLocations.GEO_DE},
          expectedUrlsSmall[i],
          expectedUrlsNormal[i],
          "",
          "https://utstreaming.zdf.de/mtt/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/F1021200_hoh_deu_Schatz_nimm_du_sie_150719.xml");
    }
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(wireMockServer.baseUrl() + aDetailUrl));
    return new ZdfFilmDetailTask(createCrawler(), wireMockServer.baseUrl(), urls, null).invoke();
  }
}
