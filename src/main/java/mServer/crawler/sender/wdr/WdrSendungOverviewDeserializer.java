package mServer.crawler.sender.wdr;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WdrSendungOverviewDeserializer extends HtmlDeserializerBase {

    private static final String QUERY_URL = "div.hideTeasertext > a";
    private static final String QUERY_URL_LOKALZEIT_VIDEO_TEASER = "div.teaser.video > a";
    private static final String HTML_ATTRIBUTE_HREF = "href";
    private static final String META_PROPERTY_THEME = "og:title";
    
    public WdrSendungOverviewDto deserialize(String urlRoot, Document document) {
        WdrSendungOverviewDto dto = new WdrSendungOverviewDto();

        dto.setTheme(this.getMetaValue(document, HtmlDeserializerBase.QUERY_META_PROPERTY, META_PROPERTY_THEME));
        
        addUrls(dto, urlRoot, document, QUERY_URL);
        addUrls(dto, urlRoot, document, QUERY_URL_LOKALZEIT_VIDEO_TEASER);
            
        return dto;
    }
    
    private void addUrls(WdrSendungOverviewDto dto, String urlRoot, Document document, String query) {
        Elements urlElements = document.select(query);
        urlElements.forEach(urlElement -> {
            String url = urlElement.attr(HTML_ATTRIBUTE_HREF);
            if(url != null && !url.isEmpty()) {
                if(url.startsWith("/")) {
                    url = urlRoot + url;
                }
                
                dto.addUrl(url);
            }
        });        
    }
}
