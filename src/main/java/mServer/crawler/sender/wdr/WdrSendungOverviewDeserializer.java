package mServer.crawler.sender.wdr;

import mServer.crawler.CrawlerTool;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrSendungOverviewDeserializer extends WdrDeserializerBase {

    private static final String QUERY_URL = "div.hideTeasertext > a";
    private static final String QUERY_URL_LOKALZEIT_VIDEO_TEASER = "div.teaser.video > a";    
    private static final String QUERY_URL_LOKALZEIT_MEHR = "h3.headline > a";
    private static final String QUERY_URL_ROCKPALAST_YEARS = "div.entries > div";
    private static final String QUERY_URL_ROCKPALAST_YEARS_ENTRIES = "div.entry > a";
    private static final String QUERY_URL_TYPE = "p.teasertext > strong";
    
    public WdrSendungDto deserialize(Document document) {
        WdrSendungDto dto = new WdrSendungDto();

        addUrls(dto, document, QUERY_URL, UrlType.VideoPage);
        addUrls(dto, document, QUERY_URL_LOKALZEIT_VIDEO_TEASER, UrlType.VideoPage);
        addUrls(dto, document, QUERY_URL_LOKALZEIT_MEHR, UrlType.VideoPage);
        
        if(checkParseRockpalastYears(document)) {
            addUrls(dto, document, QUERY_URL_ROCKPALAST_YEARS_ENTRIES, UrlType.OverviewPage);
        }
            
        return dto;
    }
    
    private void addUrls(WdrSendungDto dto, Document document, String query, UrlType defaultUrlType) {
        Elements urlElements = document.select(query);
        urlElements.forEach(urlElement -> {
            String url = urlElement.attr(HtmlDeserializerBase.HTML_ATTRIBUTE_HREF);
            if(url != null && !url.isEmpty()) {
                url = addDomainIfNecessary(url);
                
                switch(getUrlType(urlElement, defaultUrlType)) {
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
    
    private UrlType getUrlType(Element sendungElement, UrlType defaultUrlType) {
        Element typeElement = sendungElement.select(QUERY_URL_TYPE).first();

        if(typeElement != null) {
            switch(typeElement.text()) {
                case "mehr":
                    return WdrLetterPageDeserializer.UrlType.OverviewPage;
                case "video":
                    return WdrLetterPageDeserializer.UrlType.VideoPage;
            }
        }
        
        return defaultUrlType;
    }
    
    /**
     * Prüft, ob die einzelnen Jahre für die Rockpalastübersichtsseite gesucht werden sollen.
     * Das ist nur der Fall, wenn
     * - es die Rockpalaststartseite ist
     * - eine lange Suche ist
     * @param document die Seite
     * @return true, wenn die einzelnen Jahre gesucht werden sollen
     */
    private boolean checkParseRockpalastYears(Document document) {

        if(CrawlerTool.loadLongMax()) {
            // ermitteln, ob es sich um die erste Rockpalastübersichtsseite handelt
            // dazu muss das erste Element in der Jahresauswahl aktiv sein
            Elements yearElements = document.select(QUERY_URL_ROCKPALAST_YEARS);
            
            if(yearElements != null) {
                Element firstYearElement = yearElements.first();
                
                if(firstYearElement != null) {
                    return firstYearElement.classNames().contains("active");
                }
            }
        }
        
        return false;
    }
}
