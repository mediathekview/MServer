package mServer.crawler;

import java.io.IOException;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import okhttp3.mockwebserver.*;

import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AddToFilmlistTest {
    private static final String FILM_NAME_ONLINE = "onlinefilm.mp4";
    private static final String FILM_NAME_ONLINE2 = "onlinefilm2.mp4";
    private static final String FILM_TOPIC1 = "Topic 1";
    private static final String FILM_TOPIC2 = "Topic 2";
    private static final String FILM_TOPIC3 = "Topic 3";
    private static final String FILM_TITLE1 = "Title 1";
    private static final String FILM_TITLE2 = "Title 2";
    private static final String FILM_TITLE3 = "Title 3";
    
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

                switch(request.getPath()) {
                    case "/" + FILM_NAME_ONLINE:
                    case "/" + FILM_NAME_ONLINE2:
                        return new MockResponse().setResponseCode(200);
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
        
    // Test with list of 100000 different old entries which are online
    // to ensure the multithreaded onilne check is correct
    @Test
    public void testAddHugeFilmList() {
        for(int i = 0; i < 100000; i++)  {
            listToAdd.add(createTestFilm(Const.ZDF, "topic " + i, "title " + i, FILM_NAME_ONLINE));
        }

        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(100002));
    }
    
    
    private static DatenFilm createTestFilm(String sender, String topic, String title, String filmUrl) {
        DatenFilm film = new DatenFilm(sender, topic, "url", title, baseUrl + filmUrl, "", "", "", 12, "");
        film.arr[DatenFilm.FILM_GROESSE] = "10";
        
        return film;
    }
}