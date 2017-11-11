package mServer.crawler.sender.arte;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * DTO containing information of the ARTE content
 */
public class ArteInfoDTO {
    /*
    * Map of category name and the url of the category
    */
    private final Map<String, String> categoryUrls;

    public ArteInfoDTO() {
        this.categoryUrls = new HashMap<>();
    }
    
    public void addCategory(String name, String url) {
        this.categoryUrls.put(name, url);
    }
    
    public Set<String> getCategories() {
        return this.categoryUrls.keySet();
    }
    
    public String getCategoryUrl(String category) {
        return this.categoryUrls.get(category);
    }
}
