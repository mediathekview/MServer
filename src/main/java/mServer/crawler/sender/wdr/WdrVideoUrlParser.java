package mServer.crawler.sender.wdr;

import java.util.HashMap;
import java.util.Map;
import mServer.crawler.sender.newsearch.Qualities;

public class WdrVideoUrlParser {

    private static final String JS_SEARCH_ALT = "\"alt\":{\"videoURL\":\"";
    private static final String JS_SEARCH_DFLT = "\"dflt\":{\"videoURL\":\"";
    
    private static final String M3U8_RESOLUTION_BEGIN = "RESOLUTION=";
    private static final String M3U8_RESOLUTION_END = "x";
    private static final String M3U8_URL_BEGIN = "http";
    private static final String M3U8_URL_END = "m3u8";
    
    private final WdrUrlLoader urlLoader;

    public WdrVideoUrlParser(WdrUrlLoader aUrlLoader) {
        urlLoader = aUrlLoader;
    }
    
    /**
     * Ermittelt die Video-URLs für einen WDR-Film
     * @param jsUrl URL für Javascript-Dateien, die Video-URLs beinhaltet
     * @return Map mit den verfügbaren Video-Qualitäten
     */
    public WdrVideoDto parse(String jsUrl) {
        WdrVideoDto dto = new WdrVideoDto();

        String javascript = urlLoader.executeRequest(jsUrl);
        if(javascript.isEmpty()) {
            return dto;
        }

        // URL suchen
        String url = getSubstring(javascript, JS_SEARCH_ALT, "\"");
        String f4m = getSubstring(javascript, JS_SEARCH_DFLT, "\"");
                
        // Fehlendes Protokoll ergänzen, wenn es fehlt. kommt teilweise vor.
        String protocol = jsUrl.substring(0, jsUrl.indexOf(':'));
        url = addProtocolIfMissing(url, protocol);
        f4m = addProtocolIfMissing(f4m, protocol);

        if (url.endsWith(".m3u8")) {
            fillUrlsFromM3u8(dto, url);
        }
        if (!f4m.isEmpty() && url.contains("_") && url.endsWith(".mp4")) {
            fillUrlsFromf4m(dto, f4m, url);
        }
        
        dto.setSubtitleUrl(addProtocolIfMissing(getSubstring(javascript, "\"captionURL\":\"", "\""), protocol));
        
        return dto;
    }
    
    private String addProtocolIfMissing(String url, String protocol) {
        if(url.startsWith("//")) {
            return protocol + ":" + url;
        } else if(url.startsWith("://")) {
            return protocol + url;
        }

        return url;
    }
    
    private void fillUrlsFromM3u8(WdrVideoDto dto, String m3u8Url) {
        String m3u8Content = urlLoader.executeRequest(m3u8Url);

        Map<Integer, String> resolutionUrlMap = getResolutionUrlMapFromM3u8(m3u8Content);
        
        resolutionUrlMap.forEach((key, value) -> {
            if(key >= 1280)  {
                dto.addVideo(Qualities.HD, value);
            } else if(key >= 960) {
                dto.addVideo(Qualities.NORMAL, value);
            } else if(key >= 640 && dto.getUrl(Qualities.NORMAL).isEmpty()) {
                dto.addVideo(Qualities.NORMAL, value);
            } else if(key >= 512) {
                dto.addVideo(Qualities.SMALL, value);
            } else if(dto.getUrl(Qualities.SMALL).isEmpty()) {
                dto.addVideo(Qualities.SMALL, value);
            }
        });

        // Zur Sicherheit: wenn NORMAL nicht gefüllt, aber SMALL, dann NORMAL=SMALL
        if(dto.getUrl(Qualities.NORMAL).isEmpty() && !dto.getUrl(Qualities.SMALL).isEmpty()) {
            dto.addVideo(Qualities.NORMAL, dto.getUrl(Qualities.SMALL));
        }
    }
    
    private void fillUrlsFromf4m(WdrVideoDto dto, String f4m, String url) {
        // http://adaptiv.wdr.de/z/medp/ww/fsk0/104/1048369/,1048369_11885064,1048369_11885062,1048369_11885066,.mp4.csmil/manifest.f4m
        // http://ondemand-ww.wdr.de/medp/fsk0/104/1048369/1048369_11885062.mp4
        
        String urlNormal = "";
        String urlSmall = "";
        String urlHd = "";
        
        String s1 = url.substring(url.lastIndexOf('_') + 1, url.indexOf(".mp4"));
        String s2 = url.substring(0, url.lastIndexOf('_') + 1);
        try {
            int nr = Integer.parseInt(s1);
            if (f4m.contains(nr + 2 + "")) {
                urlHd = s2 + (nr + 2) + ".mp4";
            }
            if (f4m.contains(nr + 4 + "")) {
                urlSmall = s2 + (nr + 4) + ".mp4";
            }
        } catch (NumberFormatException e) {
        }
        if (!urlHd.isEmpty()) {
            if (urlSmall.isEmpty()) {
                urlSmall = urlNormal;
            }
            urlNormal = urlHd;
        }        
        
        dto.addVideo(Qualities.SMALL, urlSmall);
        dto.addVideo(Qualities.NORMAL, urlNormal);
        dto.addVideo(Qualities.HD, urlHd);
    }
    
    /**
     * Erzeugt eine Map aus der Auflösungsbreite und der Video-URL
     * @param m3u8Content Inhalt der m3u8-Datei
     * @return 
     */
    private Map<Integer, String> getResolutionUrlMapFromM3u8(String m3u8Content) {
        Map<Integer, String> resolutionUrlMap = new HashMap<>();
        
        // Split nach #, um für jede Auflösung eine eigenen String zu erhalten
        String[] parts = m3u8Content.split("#");

        for (String part : parts) {
            String resolution = getSubstring(part, M3U8_RESOLUTION_BEGIN, M3U8_RESOLUTION_END);
            String url = getSubstring(part, M3U8_URL_BEGIN, M3U8_URL_END);
            
            if(!resolution.isEmpty() && !url.isEmpty()) {
                url = M3U8_URL_BEGIN + url + M3U8_URL_END;
                int resolutionValue = Integer.parseInt(resolution);
                resolutionUrlMap.put(resolutionValue, url);
            }
        }
        
        return resolutionUrlMap;
    }

    private String getSubstring(String stringToSearchIn, String stringToStart, String stringToEnd) {
        int posStart = stringToSearchIn.indexOf(stringToStart);
        
        if(posStart < 0) {
            return "";
        }
        
        posStart = posStart + stringToStart.length();
        int posEnd = stringToSearchIn.indexOf(stringToEnd, posStart);
        
        if(posEnd > -1 && posStart <= posEnd) {
            return stringToSearchIn.substring(posStart, posEnd);
        }
        return "";
    }
}
