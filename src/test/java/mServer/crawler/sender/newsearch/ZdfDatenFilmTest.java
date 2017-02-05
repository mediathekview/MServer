package mServer.crawler.sender.newsearch;

import java.util.Arrays;
import java.util.Collection;
import mSearch.Const;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class ZdfDatenFilmTest {
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {   
            { "my topic", "url", "zdfmy topicurl" },
            { "topic", "https://rodlzdf-and-so-on.de/12/11.mp4", "zdftopichttps://rodlzdf-and-so-on.de/12/11.mp4" },
            { "topic", "https://nrodlzdf-and-so-on.de/12/11.mp4", "zdftopichttps://rodlzdf-and-so-on.de/12/11.mp4" },
            { "topic", "http://rodlzdf-and-so-on.de/12/11.mp4", "zdftopichttp://rodlzdf-and-so-on.de/12/11.mp4" },
            { "topic", "http://nrodlzdf-and-so-on.de/12/11.mp4", "zdftopichttp://nrodlzdf-and-so-on.de/12/11.mp4" },
        });
    }
    
    private final ZdfDatenFilm target;
    private final String expectedIndex;
    
    public ZdfDatenFilmTest(String topic, String url, String expectedIndex) {
        target = new ZdfDatenFilm(Const.ZDF, topic, "", "", url, "", "", "", 0, "");
        this.expectedIndex = expectedIndex;
    }
    
    @Test
    public void testGetIndex() {
        assertEquals(expectedIndex, target.getIndex());
    }
}
