package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import mSearch.Const;
import mSearch.tool.Log;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;

/**
 * jersey client of ZDF
 */
public class ZDFClient
{
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
    private static final String API_TOKEN_VIDEOS = "Bearer d2726b6c8c655e42b68b0db26131b15b22bd1a32";

    private final Client client;
    private final Gson gson;

    public ZDFClient()
    {
        client = Client.create();
        gson = new Gson();
    }

    public WebResource createSearchResource()
    {
        return createResource(ZDF_SEARCH_URL);
    }

    public WebResource createResource(String aUrl)
    {
        return client.resource(aUrl);
    }

    public JsonObject execute(WebResource webResource, ZDFClientMode aMode)
    {
        String apiToken = loadApiToken(aMode);
        ClientResponse response = webResource.header(HEADER_ACCESS_CONTROL_REQUEST_HEADERS, ACCESS_CONTROL_API_AUTH)
                .header(HEADER_ACCESS_CONTROL_REQUEST_METHOD, ACCESS_CONTROL_REQUEST_METHOD_GET)
                .header(HEADER_API_AUTH, apiToken)
                .header(HEADER_HOST, HOST)
                .header(HEADER_ORIGIN, ORIGIN)
                .header(HEADER_USER_AGENT, USER_AGENT)
                .get(ClientResponse.class);

        Log.sysLog("Lade Seite: " + webResource.getURI());

        if (response.getStatus() == 200)
        {
            return handleOk(response);
        } else
        {
            Log.errorLog(496583258, "Lade Seite " + webResource.getURI() + " fehlgeschlagen: " + response.getStatus());
            increment(RunSender.Count.FEHLER);
            return null;
        }
    }

    private String loadApiToken(final ZDFClientMode aMode)
    {
        String apiToken;
        switch (aMode)
        {
            case SEARCH:
                apiToken = API_TOKEN_SEARCH;
                break;
            case VIDEO:
                //TODO load this from https://www.zdf.de/ZDFplayer/configs/zdf/zdf2016/configuration.json
                apiToken = API_TOKEN_VIDEOS;
                break;
            default:
                apiToken = API_TOKEN_VIDEOS;
                break;
        }
        return apiToken;
    }

    private JsonObject handleOk(ClientResponse response)
    {
        increment(RunSender.Count.ANZAHL);

        String jsonOutput = response.getEntity(String.class);

        long bytes = jsonOutput.length();
        increment(RunSender.Count.SUM_DATA_BYTE, bytes);
        increment(RunSender.Count.SUM_TRAFFIC_BYTE, bytes);

        return gson.fromJson(jsonOutput, JsonObject.class);
    }

    private void increment(RunSender.Count count)
    {
        FilmeSuchen.listeSenderLaufen.inc(Const.ZDF, count);
    }

    private void increment(RunSender.Count count, long value)
    {
        FilmeSuchen.listeSenderLaufen.inc(Const.ZDF, count, value);
    }

    enum ZDFClientMode
    {
        SEARCH, VIDEO;
    }
}
