package de.mediathekview.mserver.base.config;

import java.util.Objects;

public class MServerDBConfig {
  private boolean active;
  private String url;
  private String username;
  private String password;
  private Integer refreshIntervalInDays;
  private Integer checkUrlIntervalInDays;
  private Integer batchSize;
  
  public MServerDBConfig() {
    active = false;
    url = "jdbc:postgresql://postgresMV:55432/crawler";
    username = "crawler";
    password = "secret";
    refreshIntervalInDays = 7;
    checkUrlIntervalInDays = 3;
    batchSize = 2000;
  }
  
  public MServerDBConfig(Boolean active, String url, String username, String password, int refreshIntervalInDays, int checkUrlIntervalInDays, int batchSize ) {
    this.active = active;
    this.url = url;
    this.username = username;
    this.password = password;
    this.refreshIntervalInDays = refreshIntervalInDays;
    this.checkUrlIntervalInDays = checkUrlIntervalInDays;
    this.batchSize = batchSize;
  }



  public Integer getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(Integer batchSize) {
    this.batchSize = batchSize;
  }

  public Integer getRefreshIntervalInDays() {
    return refreshIntervalInDays;
  }

  public void setRefreshIntervalInDays(Integer refreshIntervalInDays) {
    this.refreshIntervalInDays = refreshIntervalInDays;
  }

  public Integer getCheckUrlIntervalInDays() {
    return checkUrlIntervalInDays;
  }

  public void setCheckUrlIntervalInDays(Integer checkUrlIntervalInDays) {
    this.checkUrlIntervalInDays = checkUrlIntervalInDays;
  }


  public Boolean getActive() {
    return active;
  }
  public String getUrl() {
    return url;
  }
  public String getUsername() {
    return username;
  }
  public String getPassword() {
    return password;
  }
  

  public void setActive(Boolean active) {
    this.active = active;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MServerDBConfig that = (MServerDBConfig) o;

    return Objects.equals(active, that.active)
        && Objects.equals(url, that.url)
        && Objects.equals(username, that.username)
        && Objects.equals(password, that.password);
  }

  @Override
  public int hashCode() {
    return Objects.hash(active, url, username, password);
  }
}
