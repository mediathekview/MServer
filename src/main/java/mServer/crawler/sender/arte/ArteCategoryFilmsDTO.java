package mServer.crawler.sender.arte;

import java.util.ArrayList;

public class ArteCategoryFilmsDTO {

    private final ArrayList<String> programIds = new ArrayList<>();
    private int pages;
    
    public void addProgramId(String aProgramId) {
        programIds.add(aProgramId);
    }
    
    public ArrayList<String> getProgramIds() {
        return programIds;
    }

    public int getPages() {
        return pages;
    }
    
    public void setPages(int aPages) {
        pages = aPages;
    }
}
