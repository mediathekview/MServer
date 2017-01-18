package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;

import java.util.Collection;
import java.util.concurrent.RecursiveTask;
import mSearch.tool.Log;

/**
 * Task to parse the response of a search page. 
 */
public class ZDFSearchPageTask extends RecursiveTask<Collection<VideoDTO>> {

    private static final long serialVersionUID = 1L;
    
    private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";
    
    private final JsonObject searchResult;
    private final Gson gson;
    
    private static final Type ZDFENTRYDTO_COLLECTION_TYPE  = new TypeToken<Collection<ZDFEntryDTO>>()
            {
            }.getType();

    public ZDFSearchPageTask(JsonObject aSearchResult) {
        searchResult = aSearchResult;
        
        gson = new GsonBuilder()
                .registerTypeAdapter(ZDFEntryDTO.class, new ZDFEntryDTODeserializer())
                .create();
    }    
    
    @Override
    protected Collection<VideoDTO> compute() {
        
        Collection<VideoDTO> filmList = new ArrayList<>();
        Collection<ZDFEntryTask> subTasks = new ArrayList<>();
        
        Collection<ZDFEntryDTO> zdfEntryDTOList = gson.fromJson(searchResult.getAsJsonArray(JSON_ELEMENT_RESULTS), ZDFENTRYDTO_COLLECTION_TYPE);
        zdfEntryDTOList.forEach(zdfEntryDTO -> {
            final ZDFEntryTask entryTask = new ZDFEntryTask(zdfEntryDTO);
            
            /*VideoDTO dto = entryTask.invoke();
            if(dto != null) {
                filmList.add(dto);
            }*/
            entryTask.fork();
            subTasks.add(entryTask);
            Log.sysLog("EntryTask " + entryTask.hashCode() + " started.");
        });
            
        // wait till entry tasks are finished
        subTasks.forEach((task) -> {
            if(task !=  null) {
                filmList.add(task.join());
                Log.sysLog("EntryTask " + task.hashCode() + " finished.");
            } else {
                Log.sysLog("Task is null => ???");
            }
        });

        return filmList;
    }    
}
