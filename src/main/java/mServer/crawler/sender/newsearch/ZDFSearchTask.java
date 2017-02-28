package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.stream.*;
import mSearch.Config;
import mSearch.tool.Log;

public class ZDFSearchTask extends RecursiveTask<Collection<VideoDTO>>
{
    private static final String JSON_ELEMENT_NEXT = "next";
    
    private static final long serialVersionUID = 1L;
    
    private final Collection<VideoDTO> filmList;
    private final ZDFClient client;
    
    private int page;
    private final int days;
    
    public ZDFSearchTask(int aDays)
    {
        super();

        filmList = new ArrayList<>();
        client = new ZDFClient();
        page = 1;
        days = aDays;
    }

    @Override
    protected Collection<VideoDTO> compute()
    {
        if(!Config.getStop()) {
            try
            {
                Collection<ZDFSearchPageTask> subTasks = new ArrayList<>();
                JsonObject baseObject;

                do {
                    baseObject = client.executeSearch(page, days, 1);

                    if(baseObject != null) {
                        ZDFSearchPageTask task = new ZDFSearchPageTask(baseObject);
                        subTasks.add(task);
                        Log.sysLog("SearchTask " + task.hashCode() + " added.");
                    }

                    page++;
                } while(!Config.getStop() && baseObject != null && baseObject.has(JSON_ELEMENT_NEXT));            
                    filmList.addAll(invokeAll(subTasks).parallelStream()
                    .map(ForkJoinTask<Collection<VideoDTO>>::join)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())
                    );

                    Log.sysLog("All SearchTasks finished.");

            } catch (Exception ex) {
                Log.errorLog(496583201, ex);
            }
        }
        return filmList;
    }
}
