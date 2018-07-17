package mServer.crawler.sender.phoenix.parser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class PhoenixFilmXmlHandler extends DefaultHandler {

  private static final String ELEMENT_BASENAME = "basename";
  private static final String ELEMENT_AIRTIME = "airtime";
  private static final String ELEMENT_LENGTH = "length";

  private static final int ELEMENT_BASENAME_ACTIVE = 1;
  private static final int ELEMENT_AIRTIME_ACTIVE = 2;
  private static final int ELEMENT_LENGTH_ACTIVE = 3;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

  private String baseName;
  private LocalDateTime time;
  private Duration duration;

  private int activeElement;

  public String getBaseName() {
    return baseName;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public Duration getDuration() {
    return duration;
  }

  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) {
    switch(qName.toLowerCase()) {
      case ELEMENT_BASENAME:
        activeElement = ELEMENT_BASENAME_ACTIVE;
        return;
      case ELEMENT_AIRTIME:
        activeElement = ELEMENT_AIRTIME_ACTIVE;
        return;
      case ELEMENT_LENGTH:
        activeElement = ELEMENT_LENGTH_ACTIVE;
        return;
    }
  }

  @Override
  public void endElement(String uri, String localName, String qName) {
    activeElement = 0;
  }

  @Override
  public void characters(char[] ch, int start, int length) {
    if (activeElement == 0) {
      return;
    }

    final String value = new String(ch, start, length);

    switch (activeElement) {
      case ELEMENT_AIRTIME_ACTIVE:
        time = LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        return;
      case ELEMENT_BASENAME_ACTIVE:
        baseName = value;
        return;
      case ELEMENT_LENGTH_ACTIVE:
        parseDuration(value);
        return;
    }
  }

  private void parseDuration(String aValue) {
    int end = aValue.indexOf('.');
    if (end < 0) {
      end = aValue.length();
    }

    int durationValue = 0;

    final String[] parts = aValue.substring(0, end).split(":");
    for(String part: parts) {
      durationValue = durationValue * 60 + Integer.parseInt(part);
    }

    // Reduce duration by an hour, the value in the xml file is an hour too large
    duration = Duration.ofSeconds(durationValue - 3600);
  }
}
