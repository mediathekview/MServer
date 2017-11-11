package mServer.crawler.sender.hr;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Deserializes overview page of a sendung
 */
public class HrSendungOverviewDeserializer {
    
    private static final String QUERY_SENDUNG_LINK = "a.c-teaser__headlineLink";
    private static final String QUERY_SENDUNG_LINK2 = "a.c-clusterTeaser__link";
    private static final String HTML_ATTRIBUTE_HREF = "href";
    
    public List<String> deserialize(Document document) {
        List<String> urls = new ArrayList<>();
        
        addUrls(urls, document, QUERY_SENDUNG_LINK);
        addUrls(urls, document, QUERY_SENDUNG_LINK2);
        
        return urls;
    }
    
    private void addUrls(List<String> urls, Document document, String query) {
        Elements elements = document.select(query);
        elements.forEach(element -> {
            String url = element.attr(HTML_ATTRIBUTE_HREF);
            if(url != null && !url.isEmpty()) {
                urls.add(url);
            }
        });
    }
}
