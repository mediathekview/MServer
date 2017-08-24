package mServer.integrationTest.crawler.sender.zdf;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.Collection;

import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import mServer.crawler.sender.zdf.VideoDTO;
import mServer.crawler.sender.zdf.ZDFClient;
import mServer.crawler.sender.zdf.ZDFSearchPageTask;

public class ZDFIntegrationTest
{
    private static final String JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";
    private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";

    /**
     * integration test executes search request and loads one film
     */
    @Test
    public void test()
    {
        final JsonObject jsonData = executeSearch();
        final JsonObject rawFilm = reduceArray(jsonData);
        loadFilm(rawFilm);
    }

    /**
     * executes search request
     */
    private JsonObject executeSearch()
    {
        final ZDFClient target = new ZDFClient();
        final JsonObject jsonData = target.executeSearch(1, 1, 0);
        assertThat(jsonData, notNullValue());

        return jsonData;
    }

    /**
     * Searches the first valid film element.
     */
    private JsonObject reduceArray(final JsonObject aJsonData)
    {
        final JsonArray array = aJsonData.getAsJsonArray(JSON_ELEMENT_RESULTS);

        boolean hasFistElement = false;
        for (int i = array.size() - 1; i > 0; i--)
        {
            final JsonElement element = array.get(i);
            if (!hasFistElement && element.toString().contains(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT))
            {
                hasFistElement = true;
            }
            else
            {
                array.remove(i);
            }
        }
        assertThat(array.size(), greaterThan(1));
        return aJsonData;
    }

    /**
     * load film and download info
     */
    private void loadFilm(final JsonObject jsonData)
    {
        final ZDFSearchPageTask task = new ZDFSearchPageTask(jsonData);
        final Collection<VideoDTO> videos = task.invoke();

        assertThat(videos, notNullValue());
        assertThat(videos.size(), is(1));
        videos.forEach(video -> {
            assertThat(video, notNullValue());
            assertThat(video.getDownloadDto(), notNullValue());
        });
    }
}
