package mServer.crawler.sender.wdr;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrSendungOverviewDeserializer extends WdrDeserializerBase {

    private static final String QUERY_URL = "div.hideTeasertext > a";
    private static final String QUERY_URL_LOKALZEIT_VIDEO_TEASER = "div.teaser.video > a";    
    private static final String QUERY_URL_LOKALZEIT_MEHR = "h3.headline > a";
    private static final String QUERY_URL_TYPE = "p.teasertext > strong";
    
    public WdrSendungDto deserialize(Document document) {
        WdrSendungDto dto = new WdrSendungDto();

        addUrls(dto, document, QUERY_URL);
        addUrls(dto, document, QUERY_URL_LOKALZEIT_VIDEO_TEASER);
        addUrls(dto, document, QUERY_URL_LOKALZEIT_MEHR);
            
        return dto;
    }
    
    private void addUrls(WdrSendungDto dto, Document document, String query) {
        Elements urlElements = document.select(query);
        urlElements.forEach(urlElement -> {
            String url = urlElement.attr(HtmlDeserializerBase.HTML_ATTRIBUTE_HREF);
            if(url != null && !url.isEmpty()) {
                url = addDomainIfNecessary(url);
                
                switch(getUrlType(urlElement)) {
                    case OverviewPage:
                        dto.addOverviewUrls(url);
                        break;
                    case VideoPage:
                        dto.addVideoUrl(url);
                        break;
                }
            }
        });        
    }
    
    private UrlType getUrlType(Element sendungElement) {
        Element typeElement = sendungElement.select(QUERY_URL_TYPE).first();

        if(typeElement != null) {
            switch(typeElement.text()) {
                case "mehr":
                    return WdrLetterPageDeserializer.UrlType.OverviewPage;
                case "video":
                    return WdrLetterPageDeserializer.UrlType.VideoPage;
            }
        }
        
        // alles andere sind auch Videos ("Animation" zu Beginn der Seite)
        return WdrLetterPageDeserializer.UrlType.VideoPage;
    }
}
