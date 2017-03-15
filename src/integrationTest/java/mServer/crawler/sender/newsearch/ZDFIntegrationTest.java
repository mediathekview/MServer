package mServer.crawler.sender.newsearch;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ZDFIntegrationTest {
 
    private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";
    
    /**
     * integration test executes search request and loads one film
     */
    @Test
    public void test() {
        JsonObject jsonData = executeSearch();
        reduceFilms(jsonData);
        loadFilm(jsonData);
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
     * reduces elements in array to 1 to load only one film
     */
    private void reduceFilms(JsonObject jsonData) {
        JsonArray array = jsonData.getAsJsonArray(JSON_ELEMENT_RESULTS);
        for(int i = array.size() - 1; i > 0; i--)
        {
            array.remove(i);
        }
        assertThat(array.size(), is(1));              
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
