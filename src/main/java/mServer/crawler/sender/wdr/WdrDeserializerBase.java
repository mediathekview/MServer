package mServer.crawler.sender.wdr;

public abstract class WdrDeserializerBase extends HtmlDeserializerBase {
    
    private static final String UrlRoot = "http://www1.wdr.de";
    
    protected enum UrlType {
        None,
        OverviewPage,
        VideoPage
    }
    
    protected String addDomainIfNecessary(String url) {
        
        if(url != null && !url.isEmpty()) {
            if(url.startsWith("/")) {
                url = UrlRoot + url;
            }

        }
        return url;
    }
    
}
