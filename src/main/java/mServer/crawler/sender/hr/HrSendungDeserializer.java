package mServer.crawler.sender.hr;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.CrawlerTool;

public class HrSendungDeserializer {

  private static final String QUERY_BROADCAST1 = "li.c-airdates__entry";
  private static final String QUERY_BROADCAST2 = "p.byline--s";
  private static final String QUERY_DESCRIPTION = "p.copytext__text";
  private static final String QUERY_TITLE1 = "p.c-programHeader__subline";
  private static final String QUERY_TITLE2 = "span.c-contentHeader__headline";
  private static final String HTML_TAG_SOURCE = "source";
  private static final String HTML_TAG_STRONG = "strong";
  private static final String HTML_TAG_TIME = "time";
  private static final String HTML_TAG_VIDEO = "video";
  private static final String HTML_ATTRIBUTE_DATETIME = "datetime";
  private static final String HTML_ATTRIBUTE_DURATION = "data-duration";
  private static final String HTML_ATTRIBUTE_SRC = "src";

  private static final Logger LOG = LogManager.getLogger(HrSendungDeserializer.class);
  private final DateTimeFormatter dateFormatHtml =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ");
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  public Film deserialize(final String theme, final String documentUrl, final Document document)
      throws MalformedURLException {

    String date = "";
    String description;
    String time = "";
    String title;
    String videoUrl;
    long duration;

    // nur Einträge mit Video weiterverarbeiten
    videoUrl = getVideoUrl(document);
    if (videoUrl.isEmpty()) {
      return null;
    }

    final String broadcast = getBroadcast(document);
    if (!broadcast.isEmpty()) {
      try {
        final LocalDateTime d = LocalDateTime.parse(prepareBroadcast(broadcast), dateFormatHtml);
        date = d.format(dateFormatDatenFilm);
        time = d.format(timeFormatDatenFilm);
      } catch (final DateTimeParseException ex) {
        LOG.error(documentUrl, ex);
      }
    }

    title = getTitle(document);
    duration = getDuration(document);
    description = getDescription(document);

    return CrawlerTool.createFilm(Sender.HR, videoUrl, title, theme, date, time, duration,
        documentUrl, description, "", "");
  }

  private String getBroadcast(final Document document) {
    String broadcast = "";

    Element broadcastElement = document.select(QUERY_BROADCAST1).first();
    if (broadcastElement == null) {
      broadcastElement = document.select(QUERY_BROADCAST2).first();
      if (broadcastElement == null) {
        return broadcast;
      }
    }

    final Elements children = broadcastElement.children();

    for (int j = 0; j < children.size(); j++) {
      final Element child = children.get(j);

      if (child.tagName().compareToIgnoreCase(HTML_TAG_TIME) == 0) {
        broadcast = child.attr(HTML_ATTRIBUTE_DATETIME);
      }
    }

    return broadcast;
  }

  private String getDescription(final Document document) {
    String desc = "";

    final Element descElement = document.select(QUERY_DESCRIPTION).first();
    if (descElement != null) {
      final Elements children = descElement.children();
      if (children.size() > 0) {
        for (int i = 0; i < children.size(); i++) {
          if (children.get(i).tagName().compareToIgnoreCase(HTML_TAG_STRONG) == 0) {
            desc = children.get(i).text();
          }
        }
      } else {
        desc = descElement.text();
      }
    }

    return desc;
  }

  private long getDuration(final Document document) {
    String duration = "";

    final Element durationElement = document.select(HTML_TAG_VIDEO).first();
    if (durationElement != null) {
      duration = durationElement.attr(HTML_ATTRIBUTE_DURATION);
    }

    if (duration != null && !duration.isEmpty()) {
      return Long.parseLong(duration);
    }
    return 0;
  }

  private String getTitle(final Document document) {
    String title = "";

    Element titleElement = document.select(QUERY_TITLE1).first();
    if (titleElement == null) {
      titleElement = document.select(QUERY_TITLE2).first();
    }
    if (titleElement != null) {
      title = titleElement.text();
    }

    return title;
  }

  private String getVideoUrl(final Document document) {
    String url = "";

    final Element urlElement = document.select(HTML_TAG_SOURCE).first();
    if (urlElement != null) {
      url = urlElement.attr(HTML_ATTRIBUTE_SRC);
    }

    return url;
  }

  private String prepareBroadcast(String broadcast) {
    if (broadcast.length() == 10) {
      // add time
      broadcast += "T00:00+0200";
    }

    return broadcast;
  }
}
