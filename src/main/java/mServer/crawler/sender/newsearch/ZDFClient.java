package mServer.crawler.sender.newsearch;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.tool.MserverDaten;

/**
 * jersey client of ZDF
 */
public class ZDFClient {
    private static final String ZDF_SEARCH_URL = "https://api.zdf.de/search/documents";
    private static final String HEADER_ACCESS_CONTROL_REQUEST_HEADERS = "Access-Control-Request-Headers";
    private static final String HEADER_ACCESS_CONTROL_REQUEST_METHOD = "access-control-request-method";
    private static final String HEADER_API_AUTH = "api-auth";
    private static final String HEADER_HOST = "host";
    private static final String HEADER_ORIGIN = "origin";
    private static final String HEADER_USER_AGENT = "user-agent";
    private static final String ACCESS_CONTROL_API_AUTH = "api-auth";
    private static final String ACCESS_CONTROL_REQUEST_METHOD_GET = "GET";
    private static final String HOST = "api.zdf.de";
    private static final String ORIGIN = "https://www.zdf.de";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
    private static final String API_TOKEN_SEARCH = "Bearer f4ba81fa117681c42383194a7103251db2981962";
    private static final String API_TOKEN_PATTERN = "Bearer %s";

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

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
    private final ZonedDateTime today = ZonedDateTime.now().withHour(0).withMinute(0);
    private final Client client;
    private final Gson gson;


    public ZDFClient() {
        client = Client.create();
        gson = new Gson();
    }

    public JsonObject executeSearch(int page, int daysPast, int monthFuture) {
        WebResource webResource = createResource(ZDF_SEARCH_URL)
                .queryParam(PROPERTY_HAS_VIDEO, Boolean.TRUE.toString())
                .queryParam(PROPERTY_SEARCHPARAM_Q, SEARCH_ALL)
                .queryParam(PROPERTY_TYPES, TYPE_PAGE_VIDEO)
                .queryParam(PROPERTY_SORT_ORDER, SORT_ORDER_DESC)
                .queryParam(PROPERTY_DATE_FROM, today.minusDays(daysPast).format(DATE_TIME_FORMAT))
                .queryParam(PROPERTY_DATE_TO, today.plusMonths(monthFuture).format(DATE_TIME_FORMAT))
                .queryParam(PROPERTY_SORT_BY, SORT_BY_DATE)
                .queryParam(PROPERTY_PAGE, Integer.toString(page));

        return execute(webResource, ZDFClient.ZDFClientMode.SEARCH);
    }

    public JsonObject execute(String aUrl) {
        WebResource webResource = createResource(aUrl);
        return execute(webResource, ZDFClientMode.VIDEO);
    }

    private WebResource createResource(String url) {
        return client.resource(url);
    }

    private JsonObject execute(WebResource webResource, ZDFClientMode aMode) {
        String apiToken = loadApiToken(aMode);
        
        ClientResponse response = webResource.header(HEADER_ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_API_AUTH)
                .header(HEADER_ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_METHOD_GET)
                .header(HEADER_API_AUTH, apiToken)
                .header(HEADER_HOST, HOST)
                .header(HEADER_ORIGIN, ORIGIN)
                .header(HEADER_USER_AGENT, USER_AGENT)
                .get(ClientResponse.class);

        if (MserverDaten.debug)
            Log.sysLog("Lade Seite: " + webResource.getURI());

        if (response.getStatus() == 200) {
            return handleOk(response);
        } else {
            Log.errorLog(496583258, "Lade Seite " + webResource.getURI() + " fehlgeschlagen: " + response.getStatus());
            increment(RunSender.Count.FEHLER);
            return null;
        }
    }

    private String loadApiToken(final ZDFClientMode aMode) {
        String apiToken;
        switch (aMode) {
            case SEARCH:
                apiToken = API_TOKEN_SEARCH;
                break;
            case VIDEO:
                apiToken = loadApiTokenVideos();
                break;
            default:
                apiToken = loadApiTokenVideos();
                break;
        }
        return apiToken;
    }

    private String loadApiTokenVideos() {
        return String.format(API_TOKEN_PATTERN, ZDFConfigurationLoader.getInstance().loadConfig().getApiToken());
    }

    private JsonObject handleOk(ClientResponse response) {
        increment(RunSender.Count.ANZAHL);

        String jsonOutput = response.getEntity(String.class);

        long bytes = jsonOutput.length();
        increment(RunSender.Count.SUM_DATA_BYTE, bytes);
        increment(RunSender.Count.SUM_TRAFFIC_BYTE, bytes);

        return gson.fromJson(jsonOutput, JsonObject.class);
    }

    private void increment(RunSender.Count count) {
        FilmeSuchen.listeSenderLaufen.inc(Const.ZDF, count);
    }

    private void increment(RunSender.Count count, long value) {
        FilmeSuchen.listeSenderLaufen.inc(Const.ZDF, count, value);
    }

    enum ZDFClientMode {
        SEARCH, VIDEO;
    }
}
