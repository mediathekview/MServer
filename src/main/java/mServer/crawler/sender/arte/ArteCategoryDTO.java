package mServer.crawler.sender.arte;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO containing information of an ARTE category
 */
public class ArteCategoryDTO {
    
    private final String name;
    private final List<String> subcategories;
    
    public ArteCategoryDTO(String aName) {
        this.name = aName;
        this.subcategories = new ArrayList<>();
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addSubCategory(String subcategory) {
        this.subcategories.add(subcategory);
    }
    
    public List<String> getSubCategories() {
        return this.subcategories;
    }
}
