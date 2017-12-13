/*
 * BrGetFilmCountTask.java
 * 
 * Projekt    : MServer
 * erstellt am: 17.11.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br.tasks;

import java.net.URL;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.util.Strings;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.mediathekview.mlib.communication.WebAccessHelper;
import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.BrGraphQLQueries;

public class BrGetFilmCountTask implements Callable<Integer> {

    private String query;
    
    private final AbstractCrawler crawler;
    
    public BrGetFilmCountTask(AbstractCrawler crawler) {
        super();
        this.crawler = crawler;
        this.query = BrGraphQLQueries.getQuery2GetFilmCount();
    }


    @Override
    public Integer call() throws Exception {
        
        if(Strings.isEmpty(query)) {
           // TODO: Checked Exception einfügen
        }

        Integer programmCount = null;
        
        URL crawlerURLFromConfig = crawler.getRuntimeConfig().getSingleCrawlerURL(CrawlerUrlType.BR_API_URL);

        String jsonResultString = WebAccessHelper.getJsonResultFromPostAccess(crawlerURLFromConfig, String.format(query));
        
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(jsonResultString);
        
        if(element.isJsonObject()) {
            JsonObject root = element.getAsJsonObject();
            if(root.has("data")) {
                JsonObject data = root.getAsJsonObject("data");
                if(data.has("viewer")) {
                    JsonObject viewer = data.getAsJsonObject("viewer");
                    if(viewer.has("broadcastService")) {
                        JsonObject broadcastService = viewer.getAsJsonObject("broadcastService");
                        if(broadcastService.has("programmes")) {
                            JsonObject programmes = broadcastService.getAsJsonObject("programmes");
                            programmCount = programmes.get("count").getAsInt();
                        }
                    }
                }
            }

        }
        
        return programmCount;
        
    }

    
    
}
