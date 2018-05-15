package mServer.crawler.sender.arte;

import java.util.ArrayList;

public class ArteCategoryFilmsDTO {

  private final ArrayList<String> programIds = new ArrayList<>();
  private boolean hasNextPage;

  public void addProgramId(String aProgramId) {
    programIds.add(aProgramId);
  }

  public ArrayList<String> getProgramIds() {
    return programIds;
  }

  public boolean hasNextPage() {
    return hasNextPage;
  }

  public void setNextPage(boolean aNextPage) {
    hasNextPage = aNextPage;
  }
}
