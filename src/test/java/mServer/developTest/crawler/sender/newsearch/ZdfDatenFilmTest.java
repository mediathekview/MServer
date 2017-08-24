package mServer.developTest.crawler.sender.newsearch;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import de.mediathekview.mlib.daten.Film;

@RunWith(Parameterized.class)
public class ZdfDatenFilmTest
{
    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]
        {
                { "my topic", "url", "zdfmy topicurl" },
                { "topic1", "https://rodlzdf-and-so-on.de/12/11.mp4",
                        "zdftopic1https://rodlzdf-and-so-on.de/12/11.mp4" },
                { "topic2", "https://nrodlzdf-and-so-on.de/12/11.mp4",
                        "zdftopic2https://rodlzdf-and-so-on.de/12/11.mp4" },
                { "topic3", "http://rodlzdf-and-so-on.de/12/11.mp4", "zdftopic3http://rodlzdf-and-so-on.de/12/11.mp4" },
                { "topic4", "http://nrodlzdf-and-so-on.de/12/11.mp4",
                        "zdftopic4http://rodlzdf-and-so-on.de/12/11.mp4" }, });
    }

    private final Film target;
    private final String expectedIndex;

    public ZdfDatenFilmTest(final String topic, final String url, final String expectedIndex)
    {
        // target = new Film(Const.ZDF, topic, "", "", url, "", "", "", 0, "");
        // TODO
        target = null;
        this.expectedIndex = expectedIndex;
    }

    @Test
    public void testGetIndex()
    {
        assertEquals(expectedIndex, target.getIndexName());
    }
}
