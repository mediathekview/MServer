package mServer.crawler;

import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static org.junit.Assert.*;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import java.io.IOException;
import java.util.Optional;

import mServer.tool.MserverDaten;
import mServer.tool.MserverKonstanten;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

//FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
          case "/world/hls/10vor10/2023/07/10vor10_20230731_215000_19875207_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8":
          case "/ch/hls/guetnachtgschichtli/2023/07/guetnachtgschichtli_20230729_000517_19830744_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8?caption=srf/4289848a-b5d2-42c6-bf7d-2bfaf29629b1/episode/de/vod/vod.m3u8":
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
    MserverDaten.system[MserverKonstanten.SYSTEM_BANNEDFILMLIST_NR] = "file:dist/bannedFilmList.txt";
    listToAdd = new ListeFilme();
    list = new ListeFilme();
    list.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE1, "film1.mp4"));
    list.add(createTestFilm(Const.BR, FILM_TOPIC2, FILM_TITLE1, "film2.mp4"));
  }
  @Test
  public void testNotAddSameSenderTopicTitleButDifferentUrl() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));
    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
  }

  @Test
  public void testAddOldListDifferentSenderAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
  }

  @Test
  public void testAddOldListBannedFilmIgnored() {
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, FILM_TOPIC1, FILM_TITLE_FILTER, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
  }

  @Test
  public void testAddOldListDifferentTopicAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC3, FILM_TITLE1, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
  }

  @Test
  public void testAddOldListDifferentTitleAndUrlAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE2, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
  }

  @Test
  public void testAddOldListDifferentUrlNotAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE2));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
  }

  @Test
  public void testAddOldListDifferentTitleAdded() {
    listToAdd.add(createTestFilm(Const.BR, FILM_TOPIC1, FILM_TITLE3, FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
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

    assertEquals(list.size(), 2);
  }

  @Test
  public void testAddOldListOfflineButHeadRequestSuccessfulHtmlNotAdded() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video offline",
        FILM_NAME_OFFLINE_BUT_HTML_RESPONSE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
  }

  @Test
  public void testAddOldListOfflineM3U8NotAdded() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video offline",
        FILM_NAME_OFFLINE_M3U8));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
  }

  @Test
  public void testAddOldListOnlineM3U8Added() {
    listToAdd.add(createTestFilm(Const.ARD, "ard film", "video online",
        FILM_NAME_ONLINE_M3U8));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
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

    assertEquals(list.size(), 100002);
  }

  @Test
  public void testReplaceMdrAktuellTopic() {
    listToAdd.add(createTestFilm(Const.MDR, "MDR aktuell 19:30 Uhr", "MDR aktuell 19:30 Uhr", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 3);
    Optional<DatenFilm> themaStringwithoutTime = list.stream()
            .filter(film -> (film.arr[DatenFilm.FILM_THEMA].equals("MDR aktuell") && film.arr[DatenFilm.FILM_TITEL].equals("MDR aktuell 19:30 Uhr"))).findFirst();
    assertTrue(themaStringwithoutTime.isPresent());
  }

  @Test
  public void testReplaceOrfAudioDescriptionNaming() {
    listToAdd.add(createTestFilm(Const.ORF, "AD | Film", "AD | Film Testfilm", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, "AD | Film", "AD | Film ARD", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    Assertions.assertThat(list).hasSize(4)
            .anySatisfy(film -> checkFilmThemaAndTitle(film, "Film", "Film Testfilm (Audiodeskription)"))
            .anySatisfy(film -> checkFilmThemaAndTitle(film, "AD | Film", "AD | Film ARD"));
  }

  private void checkFilmThemaAndTitle(DatenFilm film, final String expectedThema, final String expectedTitle) {
    Assertions.assertThat(film.arr[DatenFilm.FILM_THEMA]).isEqualTo(expectedThema);
    Assertions.assertThat(film.arr[DatenFilm.FILM_TITEL]).isEqualTo(expectedTitle);
  }

  @Test
  public void testRemoveSrfUrlParams() {
    final DatenFilm testFilmRemoveParams = createTestFilm(Const.SRF, "10vor", "10vor1", "world/hls/10vor10/2023/07/10vor10_20230731_215000_19875207_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/hdntl=exp=1690999574~acl=%2f*~data=hdntl,0.04-1562.28~hmac=29f18372882182cc035b155825bb4772faeca5126909064987eed4e28ffa291b/index-f1-v1-a1.m3u8?start=0.04&end=1562.28&caption=srf/b2c07ad6-4904-486e-94ca-5a10745d95cb/episode/de/vod/vod.m3u8");
    testFilmRemoveParams.arr[DatenFilm.FILM_URL_KLEIN] = "275|3-v1-a1.m3u8?start=0.04&end=1562.28&caption=srf/b2c07ad6-4904-486e-94ca-5a10745d95cb/episode/de/vod/vod.m3u8";
    testFilmRemoveParams.arr[DatenFilm.FILM_URL_HD] = "275|6-v1-a1.m3u8?start=0.04&end=1562.28&caption=srf/b2c07ad6-4904-486e-94ca-5a10745d95cb/episode/de/vod/vod.m3u8";
    listToAdd.add(testFilmRemoveParams);

    final DatenFilm testFilmRemoveParamsOnlyNormal = createTestFilm(Const.SRF, "10vor", "10vor2", "world/hls/10vor10/2023/07/10vor10_20230731_215000_19875207_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/hdntl=exp=1690999574~acl=%2f*~data=hdntl,0.04-1562.28~hmac=29f18372882182cc035b155825bb4772faeca5126909064987eed4e28ffa291b/index-f1-v1-a1.m3u8?start=0.04&end=1562.28&caption=srf/b2c07ad6-4904-486e-94ca-5a10745d95cb/episode/de/vod/vod.m3u8");
    listToAdd.add(testFilmRemoveParamsOnlyNormal);

    final DatenFilm testFilmDontRemoveParams = createTestFilm(Const.SRF, "10vor", "10vor3", "ch/hls/guetnachtgschichtli/2023/07/guetnachtgschichtli_20230729_000517_19830744_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8?caption=srf/4289848a-b5d2-42c6-bf7d-2bfaf29629b1/episode/de/vod/vod.m3u8");
    listToAdd.add(testFilmDontRemoveParams);

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 5);

    assertEquals(testFilmRemoveParams.arr[DatenFilm.FILM_URL], baseUrl + "world/hls/10vor10/2023/07/10vor10_20230731_215000_19875207_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8");
    assertEquals(testFilmRemoveParams.arr[DatenFilm.FILM_URL_KLEIN], "151|3-v1-a1.m3u8");
    assertEquals(testFilmRemoveParams.arr[DatenFilm.FILM_URL_HD], "151|6-v1-a1.m3u8");
    assertEquals(testFilmRemoveParamsOnlyNormal.arr[DatenFilm.FILM_URL], baseUrl + "world/hls/10vor10/2023/07/10vor10_20230731_215000_19875207_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8");
    assertEquals(testFilmRemoveParamsOnlyNormal.arr[DatenFilm.FILM_URL_KLEIN], "");
    assertEquals(testFilmRemoveParamsOnlyNormal.arr[DatenFilm.FILM_URL_HD], "");
    assertEquals(testFilmDontRemoveParams.arr[DatenFilm.FILM_URL], baseUrl + "ch/hls/guetnachtgschichtli/2023/07/guetnachtgschichtli_20230729_000517_19830744_v_webcast_h264_,q40,q10,q20,q30,q50,q60,.mp4.csmil/index-f1-v1-a1.m3u8?caption=srf/4289848a-b5d2-42c6-bf7d-2bfaf29629b1/episode/de/vod/vod.m3u8");

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

    Assertions.assertThat(list).hasSize(5)
            .anySatisfy(film -> checkFilmThemaAndTitle(film, "Film", "Testfilm (Staffel 1) (Audiodeskription)"))
            .anySatisfy(film -> checkFilmThemaAndTitle(film, "Film", "Testfilm2 (Audiodeskription)"))
            .anySatisfy(film -> checkFilmThemaAndTitle(film, "Film mit Audiodeskription", "Testfilm mit Audiodeskription"));
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

    assertEquals(list.size(), 9);
    Optional<DatenFilm> actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("ZIB 17:00")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "ZIB");

    actual = list.stream()
            .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("ZIB 7:00")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "ZIB");

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Tagesschau 20:15")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "Tagesschau 20:15");

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("heute 19:00")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "heute 19:00");

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Guten Morgen Österrreich 08:00")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "Guten Morgen Österrreich");

    actual = list.stream()
            .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Guten Morgen Österrreich 8:30")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "Guten Morgen Österrreich");

    actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Uhrzeit 12:00 in der Mitte")).findFirst();
    assertTrue(actual.isPresent());
    assertEquals(actual.get().arr[DatenFilm.FILM_THEMA], "Uhrzeit 12:00 in der Mitte");
  }

  @Test
  public void testNotAddJugendschutzOrf() {
    listToAdd.add(createTestFilm(Const.ORF, "Tatort", "Tatort mit Jugendschutz", FILM_NAME_ORF_JUGENDSCHUTZ));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
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

    assertEquals(list.size(), 2);
  }

  @Test
  public void testNotAddArteExtraits() {
    listToAdd.add(createTestFilm(Const.ARTE_DE, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ARTE_EXTRAIT));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 2);
  }

  @Test
  public void testRefreshTitleWithTrailingDash() {
    final DatenFilm testFilmUpdated = createTestFilm(Const.ARD, "My Topic", "Title - ", FILM_NAME_ONLINE);
    final DatenFilm testFilmNotUpdated = createTestFilm(Const.ARD, "My Topic", "Title - Episode", FILM_NAME_ONLINE);

    listToAdd.add(testFilmUpdated);
    listToAdd.add(testFilmNotUpdated);

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(), 4);
    assertEquals("Title", testFilmUpdated.arr[DatenFilm.FILM_TITEL]);
    assertEquals("Title - Episode", testFilmNotUpdated.arr[DatenFilm.FILM_TITEL]);
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

    assertEquals(list.size(), 4);
    Optional<DatenFilm> sortInDifferentUrls1 = list.stream()
            .filter(film -> (film.arr[DatenFilm.FILM_WEBSEITE].equals("https://www.ardmediathek.de/video/Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM"))).findFirst();
    assertTrue(sortInDifferentUrls1.isPresent());
    Optional<DatenFilm> sortInDifferentUrls2 = list.stream()
            .filter(film -> (film.arr[DatenFilm.FILM_WEBSEITE].equals("https://www.ardmediathek.de/video/KLJpZDovL21kci5kZS9iZWl0cmFnL2Ntcy9mZjMzYzMxMC0wMjczLTQzMDktODllZi03MTI0OTFjZmE3ZTM"))).findFirst();
    assertTrue(sortInDifferentUrls2.isPresent());
  }

  @Test
  public void testArdEntriesOfZdfArteKikaRemoved() {
    final DatenFilm testFilmArd = createTestFilm(Const.ARD, "Tatort", "Test Tatort", FILM_NAME_ONLINE);
    final DatenFilm testFilmZdf = createTestFilm(Const.ARD, "Zdf", "Test Film", FILM_NAME_ONLINE);
    testFilmZdf.arr[DatenFilm.FILM_URL] = "https://tvdlzdf-a.akamaihd.net/none/zdf/23/10/231015_dk_ungeloeste_faelle_pyramiden_tex/1/231015_dk_ungeloeste_faelle_pyramiden_tex_6660k_p37v17.mp4";
    final DatenFilm testFilmArte = createTestFilm(Const.ARD, "Arte", "Test Film2", FILM_NAME_ONLINE);
    testFilmArte.arr[DatenFilm.FILM_URL] = "https://arteptweb-a.akamaihd.net/am/ptweb/109000/109800/109816-008-A_SQ_0_VF-STF_08198098_MP4-2200_AMM-PTWEB-101134210619353_29XEz10k15i.mp4";
    final DatenFilm testFilmKika = createTestFilm(Const.ARD, "Kika", "Test Film3", FILM_NAME_ONLINE);
    testFilmKika.arr[DatenFilm.FILM_URL] = "https://pmdonlinekika-a.akamaihd.net/mp4dyn/5/FCMS-5239e997-a0e9-4829-adba-ac0b4d26139b-5a2c8da1cdb7_52.mp4";
    listToAdd.add(testFilmArd);
    listToAdd.add(testFilmZdf);
    listToAdd.add(testFilmArte);
    listToAdd.add(testFilmKika);

    AddToFilmlist target =new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(),3);
    assertTrue(list.contains(testFilmArd));
    assertFalse(list.contains(testFilmArte));
    assertFalse(list.contains(testFilmKika));
    assertFalse(list.contains(testFilmZdf));
  }

  @Test
  public void testArdTagesschau24EntriesNotAdded() {
    final DatenFilm testFilmArd24 = createTestFilm(Const.ARD, "tagesschau24", "film title", FILM_NAME_ONLINE);
    final DatenFilm testFilmArdOk = createTestFilm(Const.ARD, "tagesschau", "film title", FILM_NAME_ONLINE2);
    listToAdd.add(testFilmArd24);
    listToAdd.add(testFilmArdOk);
    list.add(createTestFilm(Const.TAGESSCHAU24, "tagesschau24", "film title", FILM_NAME_ONLINE));

    AddToFilmlist target =new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(),4);
    assertFalse(list.contains(testFilmArd24));
    assertTrue(list.contains(testFilmArdOk));
    assertEquals(Const.ARD, testFilmArdOk.arr[DatenFilm.FILM_SENDER]);
  }

  @Test
  public void testArdTagesschau24UpdateSender() {
    final DatenFilm testFilmArd24 = createTestFilm(Const.ARD, "tagesschau24", "film title", FILM_NAME_ONLINE);
    listToAdd.add(testFilmArd24);

    AddToFilmlist target =new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertEquals(list.size(),3);
    assertTrue(list.contains(testFilmArd24));
    assertEquals(Const.TAGESSCHAU24, testFilmArd24.arr[DatenFilm.FILM_SENDER]);
  }

  private static DatenFilm createTestFilm(String sender, String topic, String title,
      String filmUrl) {
    DatenFilm film = new DatenFilm(sender, topic, "url", title, baseUrl + filmUrl, "", "", "", 12,
        "");
    film.arr[DatenFilm.FILM_GROESSE] = "10";

    return film;
  }
}