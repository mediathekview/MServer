package mServer.crawler.sender.newsearch;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.WebResource;
import mSearch.Config;
import mSearch.tool.Log;
import mServer.tool.MserverDaten;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

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
    
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private final Collection<VideoDTO> filmList;
    private final ZDFClient client;

    private final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);
    
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
                    baseObject = loadPage();

                    if(baseObject != null) {
                        ZDFSearchPageTask task = new ZDFSearchPageTask(baseObject);
                        subTasks.add(task);
                        if (MserverDaten.debug)
                            Log.sysLog("SearchTask " + task.hashCode() + " added.");
                    }

                    page++;
                } while(!Config.getStop() && baseObject != null && baseObject.has(JSON_ELEMENT_NEXT));            
                    filmList.addAll(invokeAll(subTasks).parallelStream()
                                    .map(ForkJoinTask::join)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList())
                    );

                if (MserverDaten.debug)
                    Log.sysLog("All SearchTasks finished.");

            } catch (Exception ex) {
                Log.errorLog(496583201, ex);
            }
        }
        return filmList;
    }
    
    private JsonObject loadPage() {
        WebResource webResource = client.createSearchResource()
                    .queryParam(PROPERTY_HAS_VIDEO, Boolean.TRUE.toString())
                    .queryParam(PROPERTY_SEARCHPARAM_Q, SEARCH_ALL)
                    .queryParam(PROPERTY_TYPES, TYPE_PAGE_VIDEO)
                    .queryParam(PROPERTY_SORT_ORDER, SORT_ORDER_DESC)
                    .queryParam(PROPERTY_DATE_FROM, today.minusDays(days).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_DATE_TO, today.plusMonths(1).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_SORT_BY, SORT_BY_DATE)
                    .queryParam(PROPERTY_PAGE, Integer.toString(page));

        if (MserverDaten.debug)
            Log.sysLog("Lade Seite: " + webResource.getURI());
                
        return client.execute(webResource);        
    }
    
}
