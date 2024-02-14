package mServer.crawler.sender.arte;

import java.util.HashSet;
import java.util.Set;

public class ArteCategoryFilmsDTO {

  private final Set<String> programIds = new HashSet<>();
  private final Set<String> collectionIds = new HashSet<>();

  private String nextPageUrl;

  public void addProgramId(String aProgramId) {
    programIds.add(aProgramId);
  }
  public void addCollection(String aCollectionId) {
    collectionIds.add(aCollectionId);
  }

  public Set<String> getProgramIds() {
    return programIds;
  }
  public Set<String> getCollectionIds() {
    return collectionIds;
  }

  public boolean hasNextPage() {
    return nextPageUrl != null && !nextPageUrl.isEmpty();
  }

  public String getNextPageUrl() {
    return nextPageUrl;
  }
  public void setNextPageUrl(String url) {
    nextPageUrl = url;
  }
}
