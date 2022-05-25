package mServer.crawler.sender.arte;

import java.util.ArrayList;

public class ArteCategoryFilmsDTO {

  private final ArrayList<String> programIds = new ArrayList<>();
  private final ArrayList<String> collectionIds = new ArrayList<>();

  private boolean hasNextPage;

  public void addProgramId(String aProgramId) {
    programIds.add(aProgramId);
  }
  public void addCollection(String aCollectionId) {
    collectionIds.add(aCollectionId);
  }

  public ArrayList<String> getProgramIds() {
    return programIds;
  }
  public ArrayList<String> getCollectionIds() {
    return collectionIds;
  }

  public boolean hasNextPage() {
    return hasNextPage;
  }

  public void setNextPage(boolean aNextPage) {
    hasNextPage = aNextPage;
  }
}
