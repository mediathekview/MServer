package mServer.crawler.sender.newsearch;

import com.sun.glass.ui.SystemClipboard;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.RecursiveTask;

public class ZDFSearchTask extends RecursiveTask<FilmDAO>
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
    private static final String APPLICATION_JSON = "application/json";
    public static final String ZDF_BASE_URL = "https://api.zdf.de//search/documents";
    public static final String HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    public static final String HEADER_ACCESS_CONTROL_REQUEST_METHOD = "access-control-request-method";
    public static final String HEADER_API_AUTH = "api-auth";
    public static final String HEADER_HOST = "host";
    public static final String HEADER_ORIGIN = "origin";
    public static final String HEADER_USER_AGENT = "user-agent";
    public static final String ACCESS_CONTROL_API_AUTH = "api-auth";
    public static final String ACCESS_CONTROL_REQUEST_METHOD_GET = "GET";
    public static final String HOST = "api.zdf.de";
    public static final String ORIGIN = "https://www.zdf.de";
    public static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
    private  static DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private int page;

    ZDFSearchTask()
    {
        page = 1;
    }

    ZDFSearchTask(int aPage)
    {
        page =aPage;
    }


    @Override
    protected FilmDAO compute()
    {
        try{
        Client client = Client.create();

        WebResource webResource = client.resource(ZDF_BASE_URL);

        final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);

        webResource.queryParam(PROPERTY_HAS_VIDEO,Boolean.TRUE.toString())
        .queryParam(PROPERTY_SEARCHPARAM_Q, SEARCH_ALL)
        .queryParam(PROPERTY_TYPES, TYPE_PAGE_VIDEO)
        .queryParam(PROPERTY_SORT_ORDER, SORT_ORDER_DESC)
        .queryParam(PROPERTY_DATE_FROM, today.minusDays(15).format(DATE_TIME_FORMAT))
        .queryParam(PROPERTY_DATE_TO,today.plusDays(3).format(DATE_TIME_FORMAT))
        .queryParam(PROPERTY_SORT_BY, SORT_BY_DATE)
        .queryParam(PROPERTY_PAGE,page);

            System.out.println(webResource.toString());


        ClientResponse response = webResource.header(HEADER_ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_API_AUTH)
                .header(HEADER_ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_METHOD_GET)
                .header(HEADER_API_AUTH,"Bearer f4ba81fa117681c42383194a7103251db2981962")
                .header(HEADER_HOST, HOST)
                .header(HEADER_ORIGIN, ORIGIN)
                .header(HEADER_USER_AGENT, USER_AGENT)
                .accept(APPLICATION_JSON).get(ClientResponse.class);

        String output = response.getEntity(String.class);
        System.out.println(output);

        if (response.getStatus() != 200)
        {
            throw new RuntimeException("Failed : HTTP error code : "
                    + response.getStatus());
        }
        } catch (Exception e) {

            e.printStackTrace();

        }
        return null;
    }

    public static void main(String... args){
            new ZDFSearchTask().compute();
    }
}
