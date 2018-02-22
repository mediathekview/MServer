package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.GeoLocations;
import java.time.Duration;
import java.time.LocalDateTime;

class WdrFilmDetailTaskTestData {
  private String requestUrl;
  private String filmPageFile;
  private String jsUrl;
  private String jsFile;
  private String m3u8Url;
  private String m3u8File;
  private String topic;
  private String expectedTitle;
  private LocalDateTime expectedDate;
  private Duration expectedDuration;
  private String expectedDescription;
  private String expectedSubtitle;
  private String expectedUrlSmall;
  private String expectedUrlNormal;
  private String expectedUrlHd;
  private GeoLocations[] expectedGeoLocations;

  public WdrFilmDetailTaskTestData(
      final String aRequestUrl,
      final String aFilmPageFile,
      final String aJsUrl,
      final String aJsFile,
      final String aM3u8Url,
      final String aM3u8File,
      final String aTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedDate,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedSubtitle,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final GeoLocations[] aExpectedGeoLocations) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    jsUrl = aJsUrl;
    jsFile = aJsFile;
    m3u8Url = aM3u8Url;
    m3u8File = aM3u8File;
    topic = aTopic;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedGeoLocations = aExpectedGeoLocations;
  }

  String getRequestUrl() {
    return requestUrl;
  }

  String getFilmPageFile() {
    return filmPageFile;
  }

  String getJsUrl() {
    return jsUrl;
  }

  String getJsFile() {
    return jsFile;
  }

  String getM3u8Url() {
    return m3u8Url;
  }

  String getM3u8File() {
    return m3u8File;
  }

  String getTopic() {
    return topic;
  }

  String getExpectedTitle() {
    return expectedTitle;
  }

  LocalDateTime getExpectedDate() {
    return expectedDate;
  }

  Duration getExpectedDuration() {
    return expectedDuration;
  }

  String getExpectedDescription() {
    return expectedDescription;
  }

  String getExpectedSubtitle() {
    return expectedSubtitle;
  }

  String getExpectedUrlSmall() {
    return expectedUrlSmall;
  }

  String getExpectedUrlNormal() {
    return expectedUrlNormal;
  }

  String getExpectedUrlHd() {
    return expectedUrlHd;
  }

  GeoLocations[] getExpectedGeoLocations() {
    return expectedGeoLocations;
  }
}
