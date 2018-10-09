package mServer.crawler.sender.hr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.ard.ArdVideoDTO;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.orf.HtmlDocumentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HrSendungDeserializer {

  private static final String QUERY_BROADCAST1 = "li.c-airdates__entry";
  private static final String QUERY_BROADCAST2 = "p.byline--s";
  private static final String QUERY_DESCRIPTION = "p.copytext__text";
  private static final String QUERY_TITLE1 = "p.c-programHeader__subline";
  private static final String QUERY_TITLE2 = "span.c-contentHeader__headline";
  private static final String HTML_TAG_SOURCE = "video > source";
  private static final String HTML_TAG_STRONG = "strong";
  private static final String HTML_TAG_TIME = "time";
  private static final String HTML_TAG_VIDEO = "video";
  private static final String HTML_TAG_VIDEO2 = "div.videoElement";
  private static final String HTML_TAG_SUBTITLE = ".c-programHeader__mediaWrapper track";
  private static final String HTML_ATTRIBUTE_DATETIME = "datetime";
  private static final String HTML_ATTRIBUTE_DURATION = "data-duration";
  private static final String HTML_ATTRIBUTE_SRC = "src";
  private static final String HTML_ATTRIBUTE_VIDEO_JSON = "data-hr-video-adaptive";

  private final DateTimeFormatter dateFormatHtml = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ");
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Logger LOG = LogManager.getLogger(HrSendungDeserializer.class);
  private static final Type OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN = new TypeToken<Optional<ArdVideoDTO>>() {
  }.getType();

  private final Gson gson;

  public HrSendungDeserializer() {
    gson = new GsonBuilder()
            .registerTypeAdapter(OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN, new HrVideoJsonDeserializer())
            .create();
  }

  public DatenFilm deserialize(String theme, String documentUrl, Document document) {

    String date = "";
    String description;
    String time = "";
    String title;
    Map<Qualities, String> videoUrls;
    String subtitle;
    long duration;

    // nur Einträge mit Video weiterverarbeiten
    videoUrls = parseVideo(document);
    if (videoUrls.isEmpty()) {
      return null;
    }

    String broadcast = getBroadcast(document);
    if (!broadcast.isEmpty()) {
      try {
        LocalDateTime d = LocalDateTime.parse(prepareBroadcast(broadcast), dateFormatHtml);
        date = d.format(dateFormatDatenFilm);
        time = d.format(timeFormatDatenFilm);
      } catch (DateTimeParseException ex) {
        LOG.error(documentUrl, ex);
      }
    }

    title = getTitle(document);
    duration = getDuration(document);
    description = getDescription(document);
    subtitle = getSubtitle(document);

    DatenFilm film = new DatenFilm(Const.HR, theme, documentUrl, title, videoUrls.get(Qualities.NORMAL), "", date, time, duration, description);
    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL), "");
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD), "");
    }
    if (!subtitle.isEmpty()) {
      CrawlerTool.addUrlSubtitle(film, subtitle);
    }

    return film;
  }

  private String prepareBroadcast(String broadcast) {
    if (broadcast.length() == 10) {
      // add time
      broadcast += "T00:00+0200";
    }

    return broadcast;
  }

  private String getBroadcast(Document document) {
    String broadcast = "";

    Element broadcastElement = document.select(QUERY_BROADCAST1).first();
    if (broadcastElement == null) {
      broadcastElement = document.select(QUERY_BROADCAST2).first();
      if (broadcastElement == null) {
        return broadcast;
      }
    }

    Elements children = broadcastElement.children();

    for (int j = 0; j < children.size(); j++) {
      Element child = children.get(j);

      if (child.tagName().compareToIgnoreCase(HTML_TAG_TIME) == 0) {
        broadcast = child.attr(HTML_ATTRIBUTE_DATETIME);
      }
    }

    return broadcast;
  }

  private String getDescription(Document document) {
    String desc = "";

    Element descElement = document.select(QUERY_DESCRIPTION).first();
    if (descElement != null) {
      Elements children = descElement.children();
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

  private long getDuration(Document document) {
    String duration = "";

    Element durationElement = document.select(HTML_TAG_VIDEO).first();
    if (durationElement != null) {
      duration = durationElement.attr(HTML_ATTRIBUTE_DURATION);
    }

    if (duration != null && !duration.isEmpty()) {
      return Long.parseLong(duration);
    }
    return 0;
  }

  private String getTitle(Document document) {
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

  private String getSubtitle(Document document) {
    String subtitle = "";

    Element subtitleElement = document.select(HTML_TAG_SUBTITLE).first();
    if (subtitleElement != null) {
      subtitle = subtitleElement.attr(HTML_ATTRIBUTE_SRC);
    }

    return subtitle;
  }

  private Map<Qualities, String> parseVideo(Document aDocument) {

    Map<Qualities, String> urls = new EnumMap<>(Qualities.class);

    // old video url
    Optional<String> videoUrl = HtmlDocumentUtils.getElementAttributeString(HTML_TAG_SOURCE, HTML_ATTRIBUTE_SRC, aDocument);
    if (videoUrl.isPresent()) {
      urls.put(Qualities.NORMAL, videoUrl.get());
    } else {
      Optional<String> videoJson = HtmlDocumentUtils.getElementAttributeString(HTML_TAG_VIDEO2, HTML_ATTRIBUTE_VIDEO_JSON, aDocument);
      if (videoJson.isPresent()) {
        Optional<ArdVideoDTO> dto = gson.fromJson(videoJson.get(), OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN);
        if (dto.isPresent()) {
          urls.putAll(dto.get().getVideoUrls());
        }
      }
    }

    return urls;
  }
}
