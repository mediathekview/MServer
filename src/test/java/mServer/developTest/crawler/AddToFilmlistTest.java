package mServer.developTest.crawler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.AddToFilmlist;
import mServer.crawler.CrawlerTool;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

public class AddToFilmlistTest
{
    private static final String FILM_NAME_ONLINE = "onlinefilm.mp4";
    private static final String FILM_NAME_ONLINE2 = "onlinefilm2.mp4";
    private static final String FILM_TOPIC1 = "Topic 1";
    private static final String FILM_TOPIC2 = "Topic 2";
    private static final String FILM_TOPIC3 = "Topic 3";
    private static final String FILM_TITLE1 = "Title 1";
    private static final String FILM_TITLE2 = "Title 2";
    private static final String FILM_TITLE3 = "Title 3";

    private static MockWebServer mockServer;
    private ListeFilme list;
    private ListeFilme listToAdd;

    @BeforeClass
    public static void setUpClass() throws IOException
    {
        mockServer = new MockWebServer();
        final Dispatcher dispatcher = new Dispatcher()
        {

            @Override
            public MockResponse dispatch(final RecordedRequest request) throws InterruptedException
            {

                switch (request.getPath())
                {
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
    }

    @AfterClass
    public static void teardownClass() throws IOException
    {
        mockServer.shutdown();
    }

    @Before
    public void setUp() throws MalformedURLException
    {
        listToAdd = new ListeFilme();
        list = new ListeFilme();
        list.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE1, "film1.mp4"));
        list.add(createTestFilm(Sender.BR, FILM_TOPIC2, FILM_TITLE1, "film2.mp4"));
    }

    @Test
    public void testAddOldListDifferentSenderAndUrlAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.ARD, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(3));
    }

    @Test
    public void testAddOldListDifferentTopicAndUrlAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC3, FILM_TITLE1, FILM_NAME_ONLINE));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(3));
    }

    @Test
    public void testAddOldListDifferentTitleAndUrlAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE2, FILM_NAME_ONLINE));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(3));
    }

    @Test
    public void testAddOldListDifferentUrlNotAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE1, FILM_NAME_ONLINE2));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(2));
    }

    @Test
    public void testAddOldListDifferentTitleAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE3, FILM_NAME_ONLINE));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(3));
    }

    @Test
    public void testAddOldListDifferentTitleAndUrlButNotOnlineNotAdded() throws MalformedURLException
    {
        listToAdd.add(createTestFilm(Sender.BR, FILM_TOPIC1, FILM_TITLE2, "imnotonline.mp4"));

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(2));
    }

    // Test with list of 100000 different old entries which are online
    // to ensure the multithreaded onilne check is correct
    @Test
    public void testAddHugeFilmList() throws MalformedURLException
    {
        for (int i = 0; i < 100000; i++)
        {
            listToAdd.add(createTestFilm(Sender.ZDF, "topic " + i, "title " + i, FILM_NAME_ONLINE));

        }

        final AddToFilmlist target = new AddToFilmlist(list, listToAdd);
        target.addOldList();

        assertThat(list.size(), equalTo(100002));
    }

    private static Film createTestFilm(final Sender sender, final String topic, final String title,
            final String filmUrl) throws MalformedURLException
    {
        final Film film = CrawlerTool.createFilm(sender, filmUrl, title, topic, "", "", 12, "url", "", "", "");
        film.addUrl(Qualities.NORMAL, new FilmUrl(new URL(filmUrl), 10l));

        return film;
    }
}