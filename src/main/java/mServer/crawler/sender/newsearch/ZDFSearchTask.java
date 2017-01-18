package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;

import com.sun.jersey.api.client.WebResource;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RecursiveTask;
import mSearch.tool.Log;

public class ZDFSearchTask extends RecursiveTask<Collection<VideoDTO>>
{
    private static final String PROPERTY_HAS_VIDEO = "hasVideo";
    private static final String PROPERTY_SEARCHPARAM_Q = "q";
    private static final String SEARCH_ALL = "*";
    private static final String PROPERTY_TYPES = "types";
    private static final String TYPE_PAGE_VIDEO = "page-video";
    private static final String PROPERTY_SORT_ORDER = "sortOrder";
    private static final String SORT_ORDER_DESC = "desc";
    private static final String PROPERTY_DATE_FROM = "from";
    private static final String PROPERTY_DATE_TO = "to";
    private static final String PROPERTY_SORT_BY = "sortBy";
    private static final String SORT_BY_DATE = "date";
    private static final String PROPERTY_PAGE = "page";
    private static final String JSON_ELEMENT_NEXT = "next";
    
    private static final long serialVersionUID = 1L;
    
    private static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private Collection<VideoDTO> filmList;
    private ZDFClient client;

    private final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);
    
    private int page;
    
    public ZDFSearchTask()
    {
        this(1);
    }

    ZDFSearchTask(int aPage)
    {
        super();

        filmList = new ArrayList<>();
        client = new ZDFClient();
        page = aPage;
    }


    @Override
    protected Collection<VideoDTO> compute()
    {
        try
        {
            Collection<ZDFSearchPageTask> subTasks = new ArrayList<>();
            JsonObject baseObject;
            
            do {
                baseObject = loadPage();

                ZDFSearchPageTask task = new ZDFSearchPageTask(baseObject);
                task.fork();
                subTasks.add(task);
                Log.sysLog("SearchTask " + task.hashCode() + " started.");
                page++;
                
            } while(baseObject.has(JSON_ELEMENT_NEXT));
            
            subTasks.forEach(task -> {
                filmList.addAll(task.join());
                Log.sysLog("SearchTask " + task.hashCode() + " finished.");
            });
           
        } catch (Exception ex) {
            Log.errorLog(496583201, ex);
        }
        return filmList;
    }
    
    private JsonObject loadPage() {
        WebResource webResource = client.createSearchResource()
                    .queryParam(PROPERTY_HAS_VIDEO, Boolean.TRUE.toString())
                    .queryParam(PROPERTY_SEARCHPARAM_Q, SEARCH_ALL)
                    .queryParam(PROPERTY_TYPES, TYPE_PAGE_VIDEO)
                    .queryParam(PROPERTY_SORT_ORDER, SORT_ORDER_DESC)
                    .queryParam(PROPERTY_DATE_FROM, today.minusDays(15).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_DATE_TO, today.plusDays(3).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_SORT_BY, SORT_BY_DATE)
                    .queryParam(PROPERTY_PAGE, Integer.toString(page));

        Log.sysLog("Lade Seite: " + webResource.getURI());
                
        return client.execute(webResource);        
    }
    
    public static void main(String... args)
    {
        new ZDFSearchTask().compute();
    }
}
