package mServer.crawler.sender.orfon;


import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrfOnBreadCrumsUrlDTO extends CrawlerUrlDTO {
  private List<String> breadCrums = new ArrayList<>();
  
  public OrfOnBreadCrumsUrlDTO(String breadCrum, String aUrl) {
    super(aUrl);
    setBreadCrums(List.of(breadCrum));
  }
  public OrfOnBreadCrumsUrlDTO(List<String> breadCrums, String aUrl) {
    super(aUrl);
    setBreadCrums(breadCrums);
  }
  
  public List<String> getBreadCrums() {
    return breadCrums;
  }
  
  public void setBreadCrums(List<String> breadCrums) {
    this.breadCrums = breadCrums;
  }

  public void setBreadCrumsPath(List<String> breadCrums) {
    List<String> fullPath = new ArrayList<>();
    fullPath.addAll(breadCrums);
    fullPath.addAll(getBreadCrums());
    setBreadCrums(fullPath);
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    if (super.equals(obj)) {
      return breadCrums.containsAll(((OrfOnBreadCrumsUrlDTO)obj).breadCrums);
    }

    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), this.breadCrums);
  }
}
