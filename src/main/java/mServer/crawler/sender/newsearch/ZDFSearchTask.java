package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.sun.jersey.api.client.WebResource;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.RecursiveTask;

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
    private static final String JSON_ELEMENT_RESULTS = "http://zdf.de/rels/search/results";
    private static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final Gson gson;

    private Collection<VideoDTO> filmList;

    private int page;
    
    private static Type zdfFilmListType  = new TypeToken<Collection<ZDFEntryDTO>>()
            {
            }.getType();

    ZDFSearchTask()
    {
        this(1);
    }

    ZDFSearchTask(int aPage)
    {
        super();

        filmList = new ArrayList<>();
        gson = new GsonBuilder()
                .registerTypeAdapter(ZDFEntryDTO.class, new ZDFEntryDTODeserializer())
                .create();

        page = aPage;
    }


    @Override
    protected Collection<VideoDTO> compute()
    {
        try
        {
            ZDFClient client = new ZDFClient();

            final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);
            WebResource webResource = client.createSearchResource()
                    .queryParam(PROPERTY_HAS_VIDEO, Boolean.TRUE.toString())
                    .queryParam(PROPERTY_SEARCHPARAM_Q, SEARCH_ALL)
                    .queryParam(PROPERTY_TYPES, TYPE_PAGE_VIDEO)
                    .queryParam(PROPERTY_SORT_ORDER, SORT_ORDER_DESC)
                    .queryParam(PROPERTY_DATE_FROM, today.minusDays(15).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_DATE_TO, today.plusDays(3).format(DATE_TIME_FORMAT))
                    .queryParam(PROPERTY_SORT_BY, SORT_BY_DATE)
                    .queryParam(PROPERTY_PAGE, Integer.toString(page));

            JsonObject baseObject = client.execute(webResource);

            Collection<ZDFEntryDTO> zdfEntryDTOList = gson.fromJson(baseObject.getAsJsonArray(JSON_ELEMENT_RESULTS), zdfFilmListType);
            for (ZDFEntryDTO zdfEntryDTO : zdfEntryDTOList)
            {
                final ZDFEntryTask entryTask = new ZDFEntryTask(zdfEntryDTO);
                entryTask.fork();
                filmList.add(entryTask.join());
            }

            
            boolean next = baseObject.has(JSON_ELEMENT_NEXT);
            if (next)
            {
                final ZDFSearchTask newTask = new ZDFSearchTask(++page);
                newTask.fork();
                filmList.addAll(newTask.join());
            }

        } catch (Exception e)
        {

            e.printStackTrace();

        }
        return filmList;
    }
    
    public static void main(String... args)
    {
        new ZDFSearchTask().compute();
    }
}
