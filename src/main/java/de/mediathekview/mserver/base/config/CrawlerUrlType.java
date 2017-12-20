/*
 * CrawlerUrlType.java
 * 
 * Projekt    : MServer
 * erstellt am: 19.11.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.base.config;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public enum CrawlerUrlType {
    BR_API_URL("https://proxy-base.master.mango.express/graphql");
    
    private URL defaultUrl;
   
    private CrawlerUrlType(String urlText) {
        try {
            if(StringUtils.isNotEmpty(urlText)) {
                this.defaultUrl = new URL(urlText);
            }
        } catch (MalformedURLException e) {
            this.defaultUrl = null;
        }
    }
    
    public Optional<URL> getDefaultUrl() {
        return Optional.ofNullable(defaultUrl);
    }
    
    
}
