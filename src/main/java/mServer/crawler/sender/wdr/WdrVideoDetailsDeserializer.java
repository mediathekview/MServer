package mServer.crawler.sender.wdr;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.MSStringBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import mServer.crawler.CrawlerTool;
import mServer.crawler.GetUrl;
import static mServer.crawler.sender.MediathekWdr.SENDERNAME;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WdrVideoDetailsDeserializer extends HtmlDeserializerBase {

    private static final String QUERY_URL = "div.videoLink > a";
    private static final String META_ITEMPROP_DESCRIPTION = "description";
    private static final String META_ITEMPROP_TITLE = "name";
    private static final String META_ITEMPROP_WEBSITE = "url";
    private static final String META_PROPERTY_DATE = "dcterms.date";
    private static final String META_PROPERTY_DURATION = "video:duration";
    
    private static final String JSON_ELEMENT_MEDIAOBJ = "mediaObj";
    private static final String JSON_ATTRIBUTE_URL = "url";
    
    private static final Logger LOG = LogManager.getLogger(WdrVideoDetailsDeserializer.class);    
    
    private static final int INDEX_URL_SMALL = 0;
    private static final int INDEX_URL_NORMAL = 1;
    private static final int INDEX_URL_HD = 2;
    private static final int INDEX_URL_SUBTITLE = 3;
    
    private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public DatenFilm deserialize(String theme, Document document) {
        
        String date = "";
        String description = getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_DESCRIPTION);
        String time = "";
        String title = getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_TITLE);
        String website = getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_WEBSITE);
        String durationString = getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_DURATION);
        long duration = 0;
        
        if(durationString != null && !durationString.isEmpty()) {
            duration = Long.parseLong(durationString);
        }
        
        String dateTime = getMetaValue(document, QUERY_META_NAME, META_PROPERTY_DATE);
        if (!dateTime.isEmpty()) {
            try {
                LocalDateTime d = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                date = d.format(dateFormatDatenFilm);
                time = d.format(timeFormatDatenFilm);
            } catch(DateTimeParseException ex) {
               LOG.error(website, ex);
            }
        }
        
        String jsUrl = getVideoJavaScriptUrl(document);
        if(jsUrl.isEmpty()) {
            return null;
        }
        
        String t = getTheme(document, title);
        if(!t.isEmpty()) {
            theme = t;
        }
        
        String[] videos = parseJs(jsUrl);
        if(!videos[INDEX_URL_NORMAL].isEmpty()) {
            DatenFilm film = new DatenFilm(Const.WDR, theme, website, title, videos[INDEX_URL_NORMAL], "", date, time, duration, description);

            if (!videos[INDEX_URL_SUBTITLE].isEmpty()) {
                CrawlerTool.addUrlSubtitle(film, videos[INDEX_URL_SUBTITLE]);
            }
            if (!videos[INDEX_URL_SMALL].isEmpty()) {
                CrawlerTool.addUrlKlein(film, videos[INDEX_URL_SMALL], "");
            }
            if (!videos[INDEX_URL_HD].isEmpty()) {
                CrawlerTool.addUrlHd(film, videos[INDEX_URL_HD], "");
            }

            return film;
        }
        
        return null;        
    }
    
    private String getTheme(Document document, String title) {
        String theme = "";
        Element titleElement = document.select("title").first();
        
        if(titleElement != null) {
            theme = titleElement.text()
                    .replace(" - Sendungen A-Z - Video - Mediathek - WDR", "")
                    .replace("- Sendung - Video - Mediathek - WDR", "")
                    .replace(title, "");
            
            if(theme.startsWith("Video:")) {
                theme = theme.substring(6).trim();
            }
            if(theme.startsWith("- ")) {
                theme = theme.substring(2).trim();
            }
        }
        
        return theme;
    }
    
    private String getVideoJavaScriptUrl(Document document) {
        // Die URL für das Video steht nicht direkt im HTML
        // stattdessen ist ein JavaScript eingebettet, dass die Video-Infos enthält
        
        String urlJs = "";
        
        Element urlElement = document.select(QUERY_URL).first();
        if(urlElement != null) {
            String extension = urlElement.attr("data-extension");
            
            JsonParser jsonParser = new JsonParser();
            JsonElement element = jsonParser.parse(extension);
            if(element != null && element.getAsJsonObject().has(JSON_ELEMENT_MEDIAOBJ)) {
                JsonElement mediaObjElement = element.getAsJsonObject().get(JSON_ELEMENT_MEDIAOBJ);
                if(mediaObjElement != null && mediaObjElement.getAsJsonObject().has(JSON_ATTRIBUTE_URL)) {
                    urlJs = mediaObjElement.getAsJsonObject().get(JSON_ATTRIBUTE_URL).getAsString();
                }
            }
        }
        
        return urlJs;
    }
    
    private String addProtocolIfMissing(String url, String protocol) {
        if(url.startsWith("//")) {
            return protocol + ":" + url;
        } else if(url.startsWith("://")) {
            return protocol + url;
        }

        return url;
    }
    
    private String getUrlFromM3u8(String m3u8Url, String qualityIndex) {
        final String CSMIL = "csmil/";
        return m3u8Url.substring(0, m3u8Url.indexOf(CSMIL)) + CSMIL + qualityIndex;
    }
    
    // TODO diese Methode gehört noch umgebaut!!!
    private String[] parseJs(String jsUrl) {
        String[] videoUrls = new String[] { "", "", "", "" };
        
        MSStringBuilder sendungsSeite4 = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        MSStringBuilder m3u8Page = new MSStringBuilder(Const.STRING_BUFFER_START_BUFFER);
        
        final String INDEX_0 = "index_0_av.m3u8"; //kleiner
        final String INDEX_1 = "index_1_av.m3u8"; //klein
        final String INDEX_2 = "index_2_av.m3u8"; //hohe Auflösung
        final GetUrl getUrl = new GetUrl(100);

        try {
            sendungsSeite4 = getUrl.getUri_Utf(Const.WDR, jsUrl, sendungsSeite4, "");
            if (sendungsSeite4.length() == 0) {
                return videoUrls;
            }
        } catch (Exception ex) {
            LOG.error(ex);
            return videoUrls;
        }
        String urlNorm, urlHd = "", urlKlein = "";

        // URL suchen
        urlNorm = sendungsSeite4.extract("\"alt\":{\"videoURL\":\"", "\"");
        String f4m = sendungsSeite4.extract("\"dflt\":{\"videoURL\":\"", "\"");

        // Fehlendes Protokoll ergänzen, wenn es fehlt. kommt teilweise vor.
        String protocol = jsUrl.substring(0, jsUrl.indexOf(':'));
        urlNorm = addProtocolIfMissing(urlNorm, protocol);
        f4m = addProtocolIfMissing(f4m, protocol);

        if (urlNorm.endsWith(".m3u8")) {
            final String urlM3 = urlNorm;
            m3u8Page = getUrl.getUri_Utf(SENDERNAME, urlNorm, m3u8Page, "");
            if (m3u8Page.indexOf(INDEX_2) != -1) {
                urlNorm = getUrlFromM3u8(urlM3, INDEX_2);
            } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                urlNorm = getUrlFromM3u8(urlM3, INDEX_1);
            }
            if (m3u8Page.indexOf(INDEX_0) != -1) {
                urlKlein = getUrlFromM3u8(urlM3, INDEX_0);
            } else if (m3u8Page.indexOf(INDEX_1) != -1) {
                urlKlein = getUrlFromM3u8(urlM3, INDEX_1);
            }

            if (urlNorm.isEmpty() && !urlKlein.isEmpty()) {
                urlNorm = urlKlein;
            }
            if (urlNorm.equals(urlKlein)) {
                urlKlein = "";
            }
        }

        if (!f4m.isEmpty() && urlNorm.contains("_") && urlNorm.endsWith(".mp4")) {
            // http://adaptiv.wdr.de/z/medp/ww/fsk0/104/1048369/,1048369_11885064,1048369_11885062,1048369_11885066,.mp4.csmil/manifest.f4m
            // http://ondemand-ww.wdr.de/medp/fsk0/104/1048369/1048369_11885062.mp4
            String s1 = urlNorm.substring(urlNorm.lastIndexOf('_') + 1, urlNorm.indexOf(".mp4"));
            String s2 = urlNorm.substring(0, urlNorm.lastIndexOf('_') + 1);
            try {
                int nr = Integer.parseInt(s1);
                if (f4m.contains(nr + 2 + "")) {
                    urlHd = s2 + (nr + 2) + ".mp4";
                }
                if (f4m.contains(nr + 4 + "")) {
                    urlKlein = s2 + (nr + 4) + ".mp4";
                }
            } catch (Exception ignore) {
            }
            if (!urlHd.isEmpty()) {
                if (urlKlein.isEmpty()) {
                    urlKlein = urlNorm;
                }
                urlNorm = urlHd;
            }
        }

        String subtitle = sendungsSeite4.extract("\"captionURL\":\"", "\"");

        videoUrls[INDEX_URL_SMALL] = urlKlein;
        videoUrls[INDEX_URL_NORMAL] = urlNorm;
        videoUrls[INDEX_URL_HD] = urlHd;
        videoUrls[INDEX_URL_SUBTITLE] = subtitle;
        
        return videoUrls;
    }
}

