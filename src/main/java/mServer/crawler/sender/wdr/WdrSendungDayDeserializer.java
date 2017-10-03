package mServer.crawler.sender.wdr;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrSendungDayDeserializer extends HtmlDeserializerBase {
    
    private static final String QUERY_URL = "div.hideTeasertext > a";
    private static final String QUERY_THEME = "h3.ressort > a";
    
    public List<WdrSendungOverviewDto> deserialize(String urlRoot, Document document) {
        List<WdrSendungOverviewDto> list = new ArrayList<>();
        
        Elements themeElements = document.select(QUERY_THEME);
        Elements urlElements = document.select(QUERY_URL);
        
        if (themeElements.size() == urlElements.size()) {
            for (int i = 0; i < themeElements.size(); i++) {
                String theme = getTheme(themeElements.get(i), urlElements.get(i));
                String url = getUrl(urlRoot, urlElements.get(i).attr(HTML_ATTRIBUTE_HREF));

                // Hilfe-URLs ignorieren
                if (!url.contains("/hilfe/")) {
                    WdrSendungOverviewDto dto = new WdrSendungOverviewDto();
                    dto.setTheme(theme);
                    dto.addUrl(url);

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
        if (theme.compareToIgnoreCase("Video") == 0
           || theme.compareToIgnoreCase("Unterhaltung") == 0
           || theme.compareToIgnoreCase("Film") == 0) {
            String[] titleParts = urlElement.attr(HTML_ATTRIBUTE_TITLE).split("-");
            if(titleParts.length >= 1) {
                theme = titleParts[0].replace(", WDR", "").trim();
            }
        }
        
        return theme;
    }
    
    private String getUrl(String urlRoot, String url) {
        
        if(url != null && !url.isEmpty()) {
            if(url.startsWith("/")) {
                url = urlRoot + url;
            }

        }
        return url;
    }
}
