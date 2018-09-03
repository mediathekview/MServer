package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mlib.daten.Resolution;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class MdrFilmXmlHandler extends DefaultHandler {

  private static final String ELEMENT_TOPIC = "broadcastseriesname";
  private static final String ELEMENT_TITLE = "title";
  private static final String ELEMENT_DESCRIPTION = "teasertext";
  private static final String ELEMENT_TIME = "broadcaststartdate";
  private static final String ELEMENT_DURATION = "duration";
  private static final String ELEMENT_FILM_URL = "progressivedownloadurl";
  private static final String ELEMENT_FILM_WIDTH = "framewidth";
  private static final String ELEMENT_FILM_HEIGHT = "frameheight";
  private static final String ELEMENT_SUBTITLE = "videosubtitleurl";
  private static final String ELEMENT_WEBSITE = "htmlurl";

  private static final int ELEMENT_TOPIC_ACTIVE = 1;
  private static final int ELEMENT_TITLE_ACTIVE = 2;
  private static final int ELEMENT_DESCRIPTION_ACTIVE = 3;
  private static final int ELEMENT_TIME_ACTIVE = 4;
  private static final int ELEMENT_DURATION_ACTIVE = 5;
  private static final int ELEMENT_FILM_URL_ACTIVE = 6;
  private static final int ELEMENT_FILMWIDTH_ACTIVE = 7;
  private static final int ELEMENT_FILMHEIGHT_ACTIVE = 8;
  private static final int ELEMENT_SUBTITLE_ACTIVE = 9;
  private static final int ELEMENT_WEBSITE_ACTIVE = 10;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private String topic = "";
  private String title = "";
  private String description = "";
  private LocalDateTime time;
  private Duration duration;
  private String subtitle;
  private String website;

  private String fileName = null;
  private int height = -1;
  private int width = -1;

  private List<MdrVideoInfo> videoInfoList = new ArrayList<>();

  private int activeElement;

  public String getTopic() {
    return topic;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public Duration getDuration() {
    return duration;
  }

  public String getSubtitle() {
    return subtitle;
  }

  public String getWebsite() {
    return website;
  }

  public String getVideoUrl(final Resolution aResolution) {
    switch (aResolution) {
      case SMALL:
        return getUrlByHeight(0, 512);
      case NORMAL:
        return getUrlByHeight(513, 960);
      case HD:
        return getUrlByHeight(961, 1920);
      default:
        return null;
    }
  }

  private String getUrlByHeight(int aMinWidth, int aMaxWidth) {
    MdrVideoInfo result = null;

    for (MdrVideoInfo videoInfo : videoInfoList) {
      if (aMinWidth <= videoInfo.getWidth() && videoInfo.getWidth() <= aMaxWidth
          && (result == null || result.getWidth() < videoInfo.getWidth())) {
        result = videoInfo;
      }
    }

    if (result != null) {
      return result.getFileName();
    }
    return null;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    switch (qName.toLowerCase()) {
      case ELEMENT_TOPIC:
        activeElement = ELEMENT_TOPIC_ACTIVE;
        return;
      case ELEMENT_TITLE:
        activeElement = ELEMENT_TITLE_ACTIVE;
        break;
      case ELEMENT_DESCRIPTION:
        activeElement = ELEMENT_DESCRIPTION_ACTIVE;
        break;
      case ELEMENT_TIME:
        activeElement = ELEMENT_TIME_ACTIVE;
        break;
      case ELEMENT_DURATION:
        activeElement = ELEMENT_DURATION_ACTIVE;
        break;
      case ELEMENT_FILM_URL:
        activeElement = ELEMENT_FILM_URL_ACTIVE;
        break;
      case ELEMENT_FILM_WIDTH:
        activeElement = ELEMENT_FILMWIDTH_ACTIVE;
        break;
      case ELEMENT_FILM_HEIGHT:
        activeElement = ELEMENT_FILMHEIGHT_ACTIVE;
        break;
      case ELEMENT_SUBTITLE:
        activeElement = ELEMENT_SUBTITLE_ACTIVE;
        break;
      case ELEMENT_WEBSITE:
        activeElement = ELEMENT_WEBSITE_ACTIVE;
        break;
      default:
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {

    if (fileName != null && !fileName.isEmpty()
        && height > 0
        && width > 0) {
      videoInfoList.add(new MdrVideoInfo(fileName, width, height));

      fileName = null;
      height = -1;
      width = -1;
    }

    activeElement = 0;
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (activeElement == 0) {
      return;
    }

    final String value = new String(ch, start, length);

    switch (activeElement) {
      case ELEMENT_TIME_ACTIVE:
        time = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        return;
      case ELEMENT_TOPIC_ACTIVE:
        topic += value;
        return;
      case ELEMENT_TITLE_ACTIVE:
        title += value;
        return;
      case ELEMENT_DESCRIPTION_ACTIVE:
        description += value;
        break;
      case ELEMENT_DURATION_ACTIVE:
        parseDuration(value);
        return;
      case ELEMENT_FILM_URL_ACTIVE:
        fileName = value;
        break;
      case ELEMENT_FILMHEIGHT_ACTIVE:
        height = Integer.parseInt(value);
        break;
      case ELEMENT_FILMWIDTH_ACTIVE:
        width = Integer.parseInt(value);
        break;
      case ELEMENT_SUBTITLE_ACTIVE:
        subtitle = value;
        break;
      case ELEMENT_WEBSITE_ACTIVE:
        if (StringUtils.isAllBlank(website)) {
          website = value;
        }
        break;
      default:
    }
  }

  private void parseDuration(String aValue) {
    int durationValue = 0;

    final String[] parts = aValue.split(":");
    for (String part : parts) {
      durationValue = durationValue * 60 + Integer.parseInt(part);
    }

    duration = Duration.ofSeconds(durationValue);
  }

}
