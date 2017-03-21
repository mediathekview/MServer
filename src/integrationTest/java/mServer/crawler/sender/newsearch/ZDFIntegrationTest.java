package mServer.crawler.sender.newsearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZDFIntegrationTest {
     private static final String JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT = "mainVideoContent";
    private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";
    
    /**
     * integration test executes search request and loads one film
     */
    @Test
    public void test() {
        JsonObject jsonData = executeSearch();
        JsonObject rawFilm = reduceArray(jsonData);
        loadFilm(rawFilm);
    } 
    
    /**
     * executes search request
     */
    private JsonObject executeSearch() {
        ZDFClient target = new ZDFClient();
        JsonObject jsonData = target.executeSearch(1, 1, 0);
        assertThat(jsonData, notNullValue());
        
        return jsonData;
    }
    
    /**
     * Searches the first valid film element.
     */
    private JsonObject reduceArray(final JsonObject aJsonData) {
        JsonArray array = aJsonData.getAsJsonArray(JSON_ELEMENT_RESULTS);
        
        boolean hasFistElement = false;
        for(int i = array.size() - 1; i > 0; i--)
        {
            JsonElement element = array.get(i);
            if(!hasFistElement && element.toString().contains(JSON_OBJ_ELEMENT_MAIN_VIDEO_CONTENT))
            {
                hasFistElement = true;
            }else {
                array.remove(i);
            }
        }
        assertThat(array.size(), greaterThan(1));   
        return aJsonData;
    }
    
    /**
     * load film and download info
     * */
    private void loadFilm(JsonObject jsonData) {
        ZDFSearchPageTask task = new ZDFSearchPageTask(jsonData);
        Collection<VideoDTO> videos = task.invoke();

        assertThat(videos, notNullValue());
        assertThat(videos.size(), is(1));
        videos.forEach(video -> {
            assertThat(video, notNullValue());
            assertThat(video.getDownloadDto(), notNullValue());
        });
    }
}
