package mServer.crawler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.core.HttpHeaders;
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
            return new MockResponse()
                .setResponseCode(200);
          case "/" + FILM_NAME_OFFLINE_BUT_HTML_RESPONSE:
            return new MockResponse()
                .setResponseCode(200)
                .addHeader(HttpHeaders.CONTENT_TYPE, "text/html; charset=UTF-8");
          case "/" + FILM_NAME_ONLINE_M3U8:
            if (request.getMethod().equalsIgnoreCase("get")) {
              return new MockResponse()
                  .setResponseCode(200)
                  .addHeader(HttpHeaders.CONTENT_TYPE,
                      "application/vnd.apple.mpegurl")
                  .addHeader(HttpHeaders.CONTENT_LENGTH, 125);
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
  public void testReplaceTimeOnlyInOrfTopic() {
    listToAdd.add(createTestFilm(Const.ORF, "ZIB 17:00", "ZIB 17:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ARD, "Tagesschau 20:15", "Tagesschau 20:15", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ZDF, "heute 19:00", "heute 19:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "Guten Morgen Österrreich 08:00", "Guten Morgen Österrreich 08:00", FILM_NAME_ONLINE));
    listToAdd.add(createTestFilm(Const.ORF, "Uhrzeit 12:00 in der Mitte", "Uhrzeit 12:00 in der Mitte", FILM_NAME_ONLINE));

    AddToFilmlist target = new AddToFilmlist(list, listToAdd);
    target.addOldList();

    assertThat(list.size(), equalTo(7));
    Optional<DatenFilm> actual = list.stream()
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("ZIB 17:00")).findFirst();
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
        .filter(film -> film.arr[DatenFilm.FILM_TITEL].equals("Uhrzeit 12:00 in der Mitte")).findFirst();
    assertTrue(actual.isPresent());
    assertThat(actual.get().arr[DatenFilm.FILM_THEMA], equalTo("Uhrzeit 12:00 in der Mitte"));
  }

  private static DatenFilm createTestFilm(String sender, String topic, String title,
      String filmUrl) {
    DatenFilm film = new DatenFilm(sender, topic, "url", title, baseUrl + filmUrl, "", "", "", 12,
        "");
    film.arr[DatenFilm.FILM_GROESSE] = "10";

    return film;
  }
}