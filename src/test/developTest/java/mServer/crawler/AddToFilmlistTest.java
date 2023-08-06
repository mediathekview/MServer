package mServer.crawler;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import java.io.IOException;
import java.util.Optional;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AddToFilmlistTest {

  private static final String FILM_NAME_ONLINE = "onlinefilm.mp4";
  private static final String FILM_NAME_ONLINE2 = "onlinefilm2.mp4";
  private static final String FILM_NAME_ONLINE_M3U8 = "onlinefilm.m3u8";
  private static final String FILM_NAME_OFFLINE_M3U8 = "offlinefilm.m3u8";
  private static final String FILM_NAME_OFFLINE_BUT_HTML_RESPONSE = "ardofflinefilm.mp4";
  private static final String FILM_NAME_ARTE_EXTRAIT = "/am/ptweb/106000/106200/106287-011-A_EXT_EQ_1_VA-STA_06707579_MP4-1500_AMM-PTWEB_EXTRAIT_1mQDhYFo2y.mp4";
  private static final String FILM_NAME_ORF_JUGENDSCHUTZ = "ipad/gp/Jugendschutz0600b2000_Q8C.mp4/playlist.m3u8";
  private static final String FILM_TOPIC1 = "Topic 1";
  private static final String FILM_TOPIC2 = "Topic 2";
  private static final String FILM_TOPIC3 = "Topic 3";
  private static final String FILM_TITLE1 = "Title 1";
  private static final String FILM_TITLE2 = "Title 2";
  private static final String FILM_TITLE3 = "Title 3";
  private static final String FILM_TITLE_FILTER = "Geschichte einer Liebe - Freya";

  private static MockWebServer mockServer;
  private static String baseUrl;

  private ListeFilme list;
  private ListeFilme listToAdd;

  @BeforeClass
  public static void setUpClass() throws IOException {
    mockServer = new MockWebServer();
    Dispatcher dispatcher = new Dispatcher() {

      @Override
      public MockResponse dispatch(RecordedRequest request) throws InterruptedException {

        switch (request.getPath()) {
          case "/" + FILM_NAME_ONLINE:
          case "/" + FILM_NAME_ONLINE2:
          case "/" + FILM_NAME_ARTE_EXTRAIT:
            return new MockResponse()
                .setResponseCode(200);
          case "/" + FILM_NAME_OFFLINE_BUT_HTML_RESPONSE:
            return new MockResponse()
                .setResponseCode(200)
                .addHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
          case "/" + FILM_NAME_ONLINE_M3U8:
            if (request.getMethod().equalsIgnoreCase("get")) {
              return new MockResponse()
                  .setResponseCode(200)
                  .addHeader(CONTENT_TYPE,
                      "application/vnd.apple.mpegurl")
                  .addHeader(CONTENT_LENGTH, 125);
            } else {
              // head not supported for m3u8
              return new MockResponse().setResponseCode(405);
            }
          case "/" + FILM_NAME_OFFLINE_M3U8:
            if (request.getMethod().equalsIgnoreCase("get")) {
              return new MockResponse().setResponseCode(404);
            } else {
              // head not supported for m3u8
              return new MockResponse().setResponseCode(405);
            }
          case "/" + FILM_NAME_ORF_JUGENDSCHUTZ:
            if (request.getMethod().equalsIgnoreCase("get")) {
              return new MockResponse().setResponseCode(200);
            } else {
              // head not supported for m3u8
              return new MockResponse().setResponseCode(405);
            }
          default:
            return new MockResponse().setResponseCode(404);
        }
      }
    };
    mockServer.setDispatcher(dispatcher);
    mockServer.start();
    baseUrl = mockServer.url("").toString();
  }

  @AfterClass
  public static void teardownClass() throws IOException {
    mockServer.shutdown();
  }

  @Before
  public void setUp() {
    listToAdd = new ListeFilme();
    list = new ListeFilme();
    list.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE1, "film1.mp4"));
    list.add(createTestFilm(Const.BR, FILM_TOPIC2, FILM_TITLE1, "film2.mp4"));
  }

  @Test
  public void testAddOldListDifferentSenderAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  @Test
  public void testAddOldListBannedFilmIgnored() {
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE_FILTER, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  @Test
  public void testAddOldListDifferentTopicAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC3, FILM_TITLE1, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  @Test
  public void testAddOldListDifferentTitleAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE2, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  @Test
  public void testAddOldListDifferentUrlNotAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE2));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testAddOldListDifferentTitleAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE3, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  @Test
  public void testAddOldListKeepArdSportschauTourDeFranceStages() {
    list.clear();
    list.add(createTestFilm(Const.ARD, "Sportschau", "3. Etappe - die komplette Übertragung", FILM_NAME_ONLINE));
    list.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));

    listToAdd.add(createTestFilm(Const.ARD, "Sportschau", "3. Etappe - die komplette Übertragung", FILM_NAME_ONLINE2));
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE2));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
  }

  @Test
  public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE2, "imnotonline.mp4"));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testAddOldListOfflineButHeadRequestSuccessfulHtmlNotAdded() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video offline",
        FILM_NAME_OFFLINE_BUT_HTML_RESPONSE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testAddOldListOfflineM3U8NotAdded() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video offline",
        FILM_NAME_OFFLINE_M3U8));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testAddOldListOnlineM3U8Added() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video online",
        FILM_NAME_ONLINE_M3U8));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
  }

  // Test with list of 100000 different old entries which are online
  // to ensure the multithreaded onilne check is correct
  @Test
  public void testAddHugeFilmList() {
    for (int i = 0; i < 100000; i++) {
      listToAdd.add(createTestFilm(Const.ZDF, "topic " + i, "title " + i, FILM_NAME_ONLINE));
    }

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(100002));
  }

  @Test
  public void testReplaceMdrAktuellTopic() {
    listToAdd.add(createTestFilm(Const.MDR, "MDR aktuell 19:30 Uhr", "MDR aktuell 19:30 Uhr", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(3));
    assertThat(list.get(2).arr[DatenFilm.FILM_THEMA], equalTo("MDR aktuell"));
    assertThat(list.get(2).arr[DatenFilm.FILM_TITEL], equalTo("MDR aktuell 19:30 Uhr"));
  }

  @Test
  public void testReplaceOrfAudioDescriptionNaming() {
    listToAdd.add(createTestFilm(Const.ORF, "AD | Film", "AD | Film Testfilm", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, "AD | Film", "AD | Film ARD", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(4));
    assertThat(list.get(2).arr[DatenFilm.FILM_THEMA], equalTo("Film"));
    assertThat(list.get(2).arr[DatenFilm.FILM_TITEL], equalTo("Film Testfilm (Audiodeskription)"));
    assertThat(list.get(3).arr[DatenFilm.FILM_THEMA], equalTo("AD | Film"));
    assertThat(list.get(3).arr[DatenFilm.FILM_TITEL], equalTo("AD | Film ARD"));
  }

  @Test
  public void testReplaceSrfAudioDescriptionNaming() {
    final DatenFilm film1 = createTestFilm(Const.SRF, "Film mit Audiodeskription", "Testfilm mit Audiodeskription (Staffel 1)", FILM_NAME_ONLINE);
    final DatenFilm film2 = createTestFilm(Const.SRF, "Film mit Audiodeskription", "Testfilm2", FILM_NAME_ONLINE);
    final DatenFilm film3 = createTestFilm(Const.ARD, "Film mit Audiodeskription", "Testfilm mit Audiodeskription", FILM_NAME_ONLINE);
    listToAdd.add(film1);
    listToAdd.add(film2);
    listToAdd.add(film3);

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(5));
    assertThat(film1.arr[DatenFilm.FILM_THEMA], equalTo("Film"));
    assertThat(film1.arr[DatenFilm.FILM_TITEL], equalTo("Testfilm (Staffel 1) (Audiodeskription)"));
    assertThat(film2.arr[DatenFilm.FILM_THEMA], equalTo("Film"));
    assertThat(film2.arr[DatenFilm.FILM_TITEL], equalTo("Testfilm2 (Audiodeskription)"));
    assertThat(film3.arr[DatenFilm.FILM_THEMA], equalTo("Film mit Audiodeskription"));
    assertThat(film3.arr[DatenFilm.FILM_TITEL], equalTo("Testfilm mit Audiodeskription"));
  }

  @Test
  public void testReplaceTimeOnlyInOrfTopic() {
    listToAdd.add(createTestFilm(Const.ORF, "ZIB 7:00", "ZIB 7:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "ZIB 17:00", "ZIB 17:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, "Tagesschau 20:15", "Tagesschau 20:15", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ZDF, "heute 19:00", "heute 19:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "Guten Morgen Österrreich 08:00", "Guten Morgen Österrreich 08:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "Guten Morgen Österrreich 8:30", "Guten Morgen Österrreich 8:30", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "Uhrzeit 12:00 in der Mitte", "Uhrzeit 12:00 in der Mitte", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(9));
    Optional<DatenFilm> actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("ZIB 17:00")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("ZIB"));

    actual = list.stream()
            .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("ZIB 7:00")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("ZIB"));

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Tagesschau 20:15")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("Tagesschau 20:15"));

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("heute 19:00")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("heute 19:00"));

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Guten Morgen Österrreich 08:00")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("Guten Morgen Österrreich"));

    actual = list.stream()
            .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Guten Morgen Österrreich 8:30")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("Guten Morgen Österrreich"));

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Uhrzeit 12:00 in der Mitte")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("Uhrzeit 12:00 in der Mitte"));
  }

  @Test
  public void testNotAddJugendschutzOrf() {
    listToAdd.add(createTestFilm(Const.ORF, "Tatort", "Tatort mit Jugendschutz", FILM_NAME_ORF_JUGENDSCHUTZ));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testNotAddOrfGeoBlockOffline() {
    DatenFilm film = new DatenFilm();
    film.arr[DatenFilm.FILM_SENDER] = Const.ORF;
    film.arr[DatenFilm.FILM_URL] = "https://apasfiis.sf.apa.at/ipad/cms-austria/2021-06-18_1330_tl_01_EM-2020--Das-wa_____14095766__o__7163875146__s14941321_1__ORF1HD_13322618P_13325906P_Q6A.mp4/playlist.m3u8";
    film.arr[DatenFilm.FILM_GROESSE]="";
    listToAdd.add(film);

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testNotAddArteExtraits() {
    listToAdd.add(createTestFilm(Const.ARTE_DE, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ARTE_EXTRAIT));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(2));
  }

  @Test
  public void testRefreshArdWebsite() {
    final DatenFilm testFilmUpdated = createTestFilm(Const.ARD, "Tatort", "Test Tatort", FILM_NAME_ONLINE);
    testFilmUpdated.arr[DatenFilm.FILM_WEBSEITE] = "https://www.ardmediathek.de/ard/player/Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM";
    listToAdd.add(testFilmUpdated);
    final DatenFilm testFilmNotUpdated = createTestFilm(Const.ARD, "Tatort", "Test Tatort", FILM_NAME_ONLINE);
    testFilmNotUpdated.arr[DatenFilm.FILM_WEBSEITE] = "https://www.ardmediathek.de/video/KLJpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM";
    listToAdd.add(testFilmNotUpdated);

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(4));
    assertThat(testFilmUpdated.arr[DatenFilm.FILM_WEBSEITE], equalTo("https://www.ardmediathek.de/video/Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM"));
    assertThat(testFilmNotUpdated.arr[DatenFilm.FILM_WEBSEITE], equalTo("https://www.ardmediathek.de/video/KLJpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM"));
  }


  private static DatenFilm createTestFilm(String sender, String topic, String title,
      String filmUrl) {
    DatenFilm film = new DatenFilm(sender, topic, "url", title, baseUrl + filmUrl, "", "", "", 12,
        "");
    film.arr[DatenFilm.FILM_GROESSE] = "10";

    return film;
  }
}