package mServer.crawler;

import java.io.IOException;
import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import okhttp3.mockwebserver.*;

import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AddToFilmlistTest {
    private static final String FILM_NAME_ONLINE = "onlinefilm.mp4";
    private static final String FILM_NAME_ONLINE2 = "onlinefilm2.mp4";
    
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
                }
                return new MockResponse().setResponseCode(404);
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
        list.add(new DatenFilm("BR", "Topic 1", "urL", "Title 1", "http://film1.mp4", "", "", "", 12, ""));
        list.add(new DatenFilm("BR", "Topic 2", "urL", "Title 1", "http://film2.mp4", "", "", "", 12, ""));
    }

    @Test
    public void testAddOldListDifferentSenderAndUrlAdded() {
        listToAdd.add(createTestFilm("ARD", "Topic 1", "urL", "Title 1", FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTopicAndUrlAdded() {
        listToAdd.add(createTestFilm("BR", "Topic 3", "urL", "Title 1", FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlAdded() {
        listToAdd.add(createTestFilm("BR", "Topic 1", "urL", "Title 2", FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentUrlNotAdded() {
        listToAdd.add(createTestFilm("BR", "Topic 1", "urL", "Title 1", FILM_NAME_ONLINE2));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
    
    @Test
    public void testAddOldListDifferentTitleAdded() {
        listToAdd.add(createTestFilm("BR", "Topic 1", "urL", "Title 3", FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() {
        listToAdd.add(createTestFilm("BR", "Topic 1", "urL", "Title 2", "imnotonline.mp4"));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
    
    private static DatenFilm createTestFilm(String sender, String topic, String website, String title, String filmUrl) {
        DatenFilm film = new DatenFilm(sender, topic, website, title, baseUrl + filmUrl, "", "", "", 12, "");
        film.arr[DatenFilm.FILM_GROESSE] = "10";
        
        return film;
    }
}