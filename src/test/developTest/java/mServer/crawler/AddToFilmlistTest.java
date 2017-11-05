    /* TODO alle Tests auskommentiert, da diese nicht an Develop angepasst sind.
            warten  mit Anpassungen bis Develop-Umstellung bzgl. Filmliste abgeschlossen sind
package mServer.crawler;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import de.mediathekview.mlib.daten.*;
import okhttp3.mockwebserver.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class AddToFilmlistTest {
    private static final Logger LOG = LogManager.getLogger(AddToFilmlistTest.class);
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
    public void setUp() throws URISyntaxException
    {
        listToAdd = new ListeFilme();
        list = new ListeFilme();
        list.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE1, "film1.mp4"));
        list.add(createTestFilm(Sender.BR, FILM_TOPIC2, FILM_TITLE1, "film2.mp4"));
    }

    @Test
    public void testAddOldListDifferentSenderAndUrlAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTopicAndUrlAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC3, FILM_TITLE1, FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE2, FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentUrlNotAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE2));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }
    
    @Test
    public void testAddOldListDifferentTitleAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE3, FILM_NAME_ONLINE));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(3));
    }
    
    @Test
    public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() throws URISyntaxException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE2, "imnotonline.mp4"));
        
        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(2));
    }    
        
    // Test with list of 100000 different old entries which are online
    // to ensure the multithreaded onilne check is correct
    @Test
    public void testAddHugeFilmList() throws URISyntaxException
    {
        for(int i = 0; i < 100000; i++)  {
                listToAdd.add(createTestFilm(Sender.ZDF, "topic " + i, "title " + i, FILM_NAME_ONLINE));

        }

        AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();
        
        assertThat(list.size(), equalTo(100002));
    }
    
    
    private static Film createTestFilm(Sender sender, String topic, String title, String filmUrl) throws URISyntaxException
    {
        Film film = CrawlerTool.createFilm(
                sender,
                filmUrl,
                title,
                topic,
                "",
                "",
                12,
                "url",
                "",
                "",
                "");
        film.addUrl(Qualities.NORMAL,new FilmUrl(new URI(filmUrl),10l));

        return film;
    }
}*/