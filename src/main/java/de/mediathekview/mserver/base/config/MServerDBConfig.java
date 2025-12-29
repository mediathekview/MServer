package de.mediathekview.mserver.base.config;

import java.util.Objects;

public class MServerDBConfig {
  private boolean active;
  private String url;
  private String username;
  private String password;
  
  public MServerDBConfig() {
    active = true;
    url = "jdbc:postgresql://postgresMV:55432/crawler";
    username = "crawler";
    password = "secret";
  }
  
  public MServerDBConfig(Boolean active, String url, String username, String password) {
    this.active = active;
    this.url = url;
    this.username = username;
    this.password = password;
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
