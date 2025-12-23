package de.mediathekview.mserver.base.config;

import java.util.Objects;

public class MServerDBConfig {
  private final Boolean active;
  private final String url;
  private final String username;
  private final String password;
  
  public MServerDBConfig() {
    super();
    active = true;
    url = "jdbc:postgresql://OscarDS:55432/crawler";
    username = "crawler";
    password = "secret";
  }
  
  public MServerDBConfig(Boolean active, String url, String username, String password) {
    super();
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
