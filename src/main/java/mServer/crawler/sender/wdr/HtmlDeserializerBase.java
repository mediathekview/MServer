package mServer.crawler.sender.wdr;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlDeserializerBase {
    
    public static final String QUERY_META_ITEMPROP = "meta[itemprop=%s]";
    public static final String QUERY_META_NAME = "meta[name=%s]";
    public static final String QUERY_META_PROPERTY = "meta[property=%s]";
    private static final String HTML_ATTRIBUTE_CONTENT = "content";
    
    protected String getMetaValue(Document document, String query, String itempropName) {
        String value = "";
        query = String.format(query, itempropName);
        Element element = document.select(query).first();
        if (element != null) {
            value = element.attr(HTML_ATTRIBUTE_CONTENT);
        }
        return value;
    }
    
}
