package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class ZdfFilmDetailTaskMultipleLanguagesTest extends ZdfTaskTestBase {

  @Test
  void audioLanguageIsOnlySetForAThreeLetterCode() throws Exception {
    // Das Feld sichert den dreistelligen ISO-639-2/T-Code zu. Die Tracks des ZDF sind bisher
    // ausnahmslos so ausgezeichnet - kaeme doch einmal ein zweistelliger Code, darf er nicht
    // ungeprueft in der Filmliste landen.
    final java.lang.reflect.Method setAudioLanguage =
        ZdfFilmDetailTask.class.getDeclaredMethod("setAudioLanguage", String.class, Film.class);
    setAudioLanguage.setAccessible(true);

    assertThat(audioLanguageFor(setAudioLanguage, "eng"), equalTo("eng"));
    assertThat(audioLanguageFor(setAudioLanguage, "fra"), equalTo("fra"));
    // Deutsch ist die uebliche Fassung und bleibt leer.
    assertThat(audioLanguageFor(setAudioLanguage, "deu"), nullValue());
    // Die Art der Tonspur ist keine Sprache - Suffix wird abgeschnitten.
    assertThat(audioLanguageFor(setAudioLanguage, "deu-ad"), nullValue());
    assertThat(audioLanguageFor(setAudioLanguage, "eng-ad"), equalTo("eng"));
    // Alles, was kein dreistelliger Buchstabencode ist, ergibt nichts.
    assertThat(audioLanguageFor(setAudioLanguage, "de"), nullValue());
    assertThat(audioLanguageFor(setAudioLanguage, "en"), nullValue());
    assertThat(audioLanguageFor(setAudioLanguage, "123"), nullValue());
  }

  private static String audioLanguageFor(
      final java.lang.reflect.Method setAudioLanguage, final String language) throws Exception {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.ZDF,
            "Titel",
            "Thema",
            LocalDateTime.of(2026, 1, 1, 20, 15),
            Duration.ofMinutes(90));
    setAudioLanguage.invoke(null, language, film);
    return film.getAudioLanguage();
  }

  @Test
  void audioLanguageIsKeptForTheNonGermanVersion() {
    // Das ZDF fuehrt die Downloads je Sprache und kennt den Code bereits als "eng". Bisher wurde
    // daraus nur der Titelzusatz "(Englisch)"; der Code selbst war danach nicht mehr vorhanden.
    final String filmUrl = "/content/documents/zdf/serien/hardsun/hard-sun-1-100.json";
    final String videoUrl = "/tmd/2/android_native_5/vod/ptmd/mediathek/180416_2215_sendung_hsn";
    setupSuccessfulJsonResponse(filmUrl, "/zdf/zdf_film_details_english.json");
    setupSuccessfulJsonResponse(videoUrl, "/zdf/zdf_video_details_english.json");
    setupHeadResponse(404);

    final Set<Film> actual = executeTask(filmUrl);

    final Film german =
        actual.stream().filter(f -> !f.getTitel().contains("(Englisch)")).findFirst().orElseThrow();
    final Film english =
        actual.stream().filter(f -> f.getTitel().contains("(Englisch)")).findFirst().orElseThrow();

    assertThat(english.getAudioLanguage(), equalTo("eng"));
    // Die deutsche Fassung bleibt leer - wie ihr Titel, der ebenfalls keinen Zusatz bekommt.
    assertThat(german.getAudioLanguage(), nullValue());
  }

  @Test
  void testGermanAndEnglish() {

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
