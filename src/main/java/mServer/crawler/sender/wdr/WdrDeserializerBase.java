package mServer.crawler.sender.wdr;

public abstract class WdrDeserializerBase extends HtmlDeserializerBase {
    
    private static final String URL_ROOT = "http://www1.wdr.de";
    
    protected enum UrlType {
        None,
        OverviewPage,
        VideoPage
    }
    
    protected String addDomainIfNecessary(String url) {
        
        if(url != null && !url.isEmpty()) {
            if(url.startsWith("/")) {
                url = URL_ROOT + url;
            }

        }
        return url;
    }
    
}
