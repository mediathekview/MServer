package mServer.crawler.sender.hr;

import java.util.ArrayList;
import java.util.List;
import mServer.crawler.sender.MediathekReader;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Deserializes http://www.hr-fernsehen.de/sendungen-a-z/index.html
 */
public class HrSendungenListDeserializer {
    
    private static final String QUERY_SENDUNG_LINK = "a.c-teaser__headlineLink";
    private static final String QUERY_SENDUNG_THEME = "span.c-teaser__headline";
    private static final String HTML_ATTRIBUTE_HREF = "href";
    
    public List<HrSendungenDto> deserialize(Document document) {
        List<HrSendungenDto> dtos = new ArrayList<>();
        
        Elements elements = document.select(QUERY_SENDUNG_LINK);
        elements.forEach(element -> {
            String theme = "";
            String url = element.attr(HTML_ATTRIBUTE_HREF);
            Element headlineElement = element.select(QUERY_SENDUNG_THEME).first();
            if(headlineElement != null) {
                theme = headlineElement.text();
            }
            
            // HR-Sendungen im Ersten ignorieren
            // erste schnelle Lösung: URLs mit daserste ignorieren
            if(!url.contains("daserste")) {
                HrSendungenDto dto = new HrSendungenDto();
                dto.setTheme(theme);
                dto.setUrl(prepareUrl(theme, url));

                dtos.add(dto);
            }
        });
        
        return dtos;
    }
    
    /**
     * URL anpassen, so dass diese direkt die Übersicht der Folgen beinhaltet,
     * sofern diese Seite existiert!
     * Damit wird das unnötige Einlesen einer Zwischenseite gespart.
     * @param theme Thema der Sendung
     * @param url URL zu Startseite der Sendung
     * @return URL zu der Folgenübersicht der Sendung
     */
    private String prepareUrl(String theme, String url) {
        // Sonderseite für Hessenschau verwenden
        if (theme.contains("hessenschau")) {
            return "http://www.hessenschau.de/tv-sendung/sendungsarchiv/index.html";
        } 

        // bei allen anderen, probieren, ob eine URL mit "sendungen" vor index.html existiert
        String preparedUrl = url.replaceAll("index.html", "sendungen/index.html");
        if (MediathekReader.urlExists(preparedUrl)) {
            return preparedUrl;
        }
        
        return url;
    }
}
