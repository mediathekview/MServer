package mServer.crawler.sender.newsearch;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Deserializes index page to determine the bearers to use for api calls
 */
public class ZdfIndexPageDeserializer {
    
    private static final String QUERY_SEARCH_BEARER = "head > script"; 
    private static final String QUERY_VIDEO_BEARER = "article > script"; 
    private static final String JSON_API_TOKEN = "apiToken";
    
    public ZDFConfigurationDTO deserialize(Document document) {
        ZDFConfigurationDTO config = new ZDFConfigurationDTO();
        
        config.setApiToken(ZDFClient.ZDFClientMode.SEARCH, parseSearchBearer(document));
        config.setApiToken(ZDFClient.ZDFClientMode.VIDEO, parseVideoBearer(document));
        
        return config;
    }
    
    private String parseVideoBearer(Document document) {
        String bearer = "";
        
        Elements scriptElements = document.select(QUERY_VIDEO_BEARER);
        for(Element scriptElement : scriptElements) {
            String script = scriptElement.html();
            
            String value = parseBearer(script, "\"");
            if(!value.isEmpty()) {
                bearer = value;
            }
        }
        
        return bearer;
    }
    
    private String parseSearchBearer(Document document) {
        String bearer = "";
        
        Elements scriptElements = document.select(QUERY_SEARCH_BEARER);
        for(Element scriptElement : scriptElements) {
            String script = scriptElement.html();
            
            String value = parseBearer(script, "'");
            if(!value.isEmpty()) {
                bearer = value;
            }
        }
        
        return bearer;
    }
    
    private String parseBearer(String json, String stringQuote) {
        String bearer = "";
    
        int indexToken = json.indexOf(JSON_API_TOKEN);
            
        if(indexToken > 0) {
            int indexStart = json.indexOf(stringQuote, indexToken + JSON_API_TOKEN.length() + 1) + 1;
            int indexEnd = json.indexOf(stringQuote, indexStart);

            if(indexStart > 0) {
                bearer = json.substring(indexStart, indexEnd);
            }        
        }
        
        return bearer;
    }
}
