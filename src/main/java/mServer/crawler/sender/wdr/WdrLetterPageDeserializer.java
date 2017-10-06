package mServer.crawler.sender.wdr;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrLetterPageDeserializer extends WdrDeserializerBase {
    
    private static final String QUERY_SENDUNG = "ul.list > li > a";
    private static final String QUERY_TITLE = "span";
    private static final String QUERY_URL_TYPE = "strong";
    
    public List<WdrSendungDto> deserialize(Document document) {
        List<WdrSendungDto> list = new ArrayList<>();
        
        Elements sendungenElement = document.select(QUERY_SENDUNG);
        sendungenElement.forEach(sendungElement -> {
            String url = getUrl(sendungElement);
                    
            if(!url.isEmpty()) {
                WdrSendungDto dto = new WdrSendungDto();
                dto.setTheme(getTheme(sendungElement));

                switch(getUrlType(sendungElement)) {
                    case OverviewPage:
                        dto.addOverviewUrls(url);
                        break;
                    case VideoPage:
                        dto.addVideoUrl(url);
                        break;
                }
                
                list.add(dto);
            }
        });
        
        return list;
    }
    
    private String getTheme(Element sendungElement) {
        
        Element titleElement = sendungElement.select(QUERY_TITLE).first();
        
        if(titleElement != null) {
            return titleElement.text();
        }
        
        return "";
    }
    
    private String getUrl(Element sendungElement) {
        String url = sendungElement.attr(HTML_ATTRIBUTE_HREF);
        
        if(!url.isEmpty()) {
            url = addDomainIfNecessary(url);
        }
        
        return url;
    }
    
    private UrlType getUrlType(Element sendungElement) {
        Element typeElement = sendungElement.select(QUERY_URL_TYPE).first();

        if(typeElement != null) {
            switch(typeElement.text()) {
                case "mehr":
                    return UrlType.OverviewPage;
                case "video":
                    return UrlType.VideoPage;
            }
        }
        
        return UrlType.None;
    }
}
