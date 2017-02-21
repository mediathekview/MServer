package mServer.crawler.sender.newsearch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import mSearch.tool.Log;

/**
 * A simple singelton to read the ZDF configuration just once per runtime.
 */
public class ZDFConfigurationLoader
{
    public static final String ZDF_CONFIGURATION_URL = "https://www.zdf.de/ZDFplayer/configs/zdf/zdf2016/configuration.json";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:50.0) Gecko/20100101 Firefox/50.0";
    private static final String HEADER_USER_AGENT = "user-agent";
    private static ZDFConfigurationLoader instance;

    private ZDFConfigurationDTO config;

    private ZDFConfigurationLoader()
    {
        config = null;
    }

    public static ZDFConfigurationLoader getInstance()
    {
        if (instance == null)
        {
            instance = new ZDFConfigurationLoader();
        }
        return instance;
    }

    public ZDFConfigurationDTO loadConfig()
    {
        if (config == null)
        {
            WebResource webResource = Client.create().resource(ZDF_CONFIGURATION_URL);
            ClientResponse response = webResource
                    .header(HEADER_USER_AGENT, USER_AGENT)
                    .get(ClientResponse.class);

            if (response.getStatus() == 200)
            {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(ZDFConfigurationDTO.class, new ZDFConfigurationDTODeserializer())
                        .create();
                config = gson.fromJson(response.getEntity(String.class), ZDFConfigurationDTO.class);
            } else
            {
                Log.errorLog(496583428, "Lade der Config Seite " + webResource.getURI() + " fehlgeschlagen: " + response.getStatus());
                config = new ZDFConfigurationDTO("");
            }

        }
        return config;
    }
}
