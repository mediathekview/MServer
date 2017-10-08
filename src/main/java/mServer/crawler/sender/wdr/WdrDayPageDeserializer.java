package mServer.crawler.sender.wdr;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrDayPageDeserializer extends WdrDeserializerBase {
    
    private static final String QUERY_URL = "div.hideTeasertext > a";
    private static final String QUERY_THEME = "h3.ressort > a";
    
    public List<WdrSendungDto> deserialize(Document document) {
        List<WdrSendungDto> list = new ArrayList<>();
        
        Elements themeElements = document.select(QUERY_THEME);
        Elements urlElements = document.select(QUERY_URL);
        
        if (themeElements.size() == urlElements.size()) {
            for (int i = 0; i < themeElements.size(); i++) {
                String theme = getTheme(themeElements.get(i), urlElements.get(i));
                String url = addDomainIfNecessary(urlElements.get(i).attr(HTML_ATTRIBUTE_HREF));

                // Hilfe-URLs ignorieren
                if (!url.contains("/hilfe/")) {
                    WdrSendungDto dto = new WdrSendungDto();
                    dto.setTheme(theme);
                    dto.addVideoUrl(url);

                    list.add(dto);
                }
            }
        } else {
            list.clear();
        }
        
        return list;
    }
    
    private String getTheme(Element themeElement, Element urlElement) {
        String theme = themeElement.text();
        
        // Sonderbehandlung für Thema: bei bestimmten Wörtern das Thema aus Videotitel ermitteln
        if(theme.compareToIgnoreCase("Film") == 0) {
            // Aus Film -> Fernsehfilm machen, damit das Thema zu Sendung A-Z passt
            theme = "Fernsehfilm";
        } else if (theme.compareToIgnoreCase("Video") == 0) {
            String[] titleParts = urlElement.attr(HTML_ATTRIBUTE_TITLE).split("-");
            if(titleParts.length >= 1) {
                theme = titleParts[0].replace(", WDR", "").trim();
            }
        } 
        
        return theme;
    }
}
