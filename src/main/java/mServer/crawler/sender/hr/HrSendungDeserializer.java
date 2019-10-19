package mServer.crawler.sender.hr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.base.HtmlDocumentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;

public class HrSendungDeserializer {

  private static final String QUERY_BROADCAST1 = "li.c-airdates__entry";
  private static final String QUERY_BROADCAST2 = "p.byline--s";
  private static final String QUERY_DESCRIPTION = "p.copytext__text";
  private static final String QUERY_TITLE1 = "p.c-programHeader__subline";
  private static final String QUERY_TITLE2 = "span.c-contentHeader__headline";
  private static final String HTML_TAG_STRONG = "strong";
  private static final String HTML_TAG_TIME = "time";
  private static final String HTML_TAG_VIDEO2 = "figure .js-loadScript";
  private static final String HTML_ATTRIBUTE_DATETIME = "datetime";
  private static final String HTML_ATTRIBUTE_VIDEO_JSON = "data-hr-mediaplayer-loader";
  private static final String DAUER_SELECTOR1 = ".c-programHeader__mediaWrapper .mediaInfo__byline";
  private static final String DAUER_SELECTOR2 = ".c-contentHeader .mediaInfo__byline";

  private final DateTimeFormatter dateFormatHtml = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmZ");
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  private static final Logger LOG = LogManager.getLogger(HrSendungDeserializer.class);
  private static final Type OPTIONAL_HRVIDEOINFODTO_TYPE_TOKEN = new TypeToken<Optional<HrVideoInfoDto>>() {
  }.getType();

  private final Gson gson;

  public HrSendungDeserializer() {
    gson = new GsonBuilder()
            .registerTypeAdapter(OPTIONAL_HRVIDEOINFODTO_TYPE_TOKEN, new HrVideoJsonDeserializer())
            .create();
  }

  public DatenFilm deserialize(String theme, String documentUrl, Document document) {

    String date = "";
    String description;
    String time = "";
    String title;
    Optional<HrVideoInfoDto> videoInfoDto;
    long duration;

    // nur Einträge mit Video weiterverarbeiten
    videoInfoDto = parseVideo(document);
    if (!videoInfoDto.isPresent() || videoInfoDto.get().getVideoUrls().isEmpty()) {
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

    final Map<Qualities, String> videoUrls = videoInfoDto.get().getVideoUrls();

    DatenFilm film = new DatenFilm(Const.HR, theme, documentUrl, title, videoUrls.get(Qualities.NORMAL), "", date, time, duration, description);
    if (videoUrls.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videoUrls.get(Qualities.SMALL));
    }
    if (videoUrls.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videoUrls.get(Qualities.HD));
    }
    if (videoInfoDto.get().getSubtitle().isPresent()) {
      CrawlerTool.addUrlSubtitle(film, videoInfoDto.get().getSubtitle().get());
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

    final Optional<String> dauerText
            = HtmlDocumentUtils.getElementString(DAUER_SELECTOR1, DAUER_SELECTOR2, document);
    if (dauerText.isPresent()) {
      Optional<Duration> duration = HtmlDocumentUtils.parseDuration(dauerText);
      if (duration.isPresent()) {
        Duration durationValue = duration.get();
        return durationValue.getSeconds();
      }
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

  private Optional<HrVideoInfoDto> parseVideo(Document aDocument) {
    Optional<String> videoJson = HtmlDocumentUtils.getElementAttributeString(HTML_TAG_VIDEO2, HTML_ATTRIBUTE_VIDEO_JSON, aDocument);
    if (videoJson.isPresent()) {
      return gson.fromJson(videoJson.get(), OPTIONAL_HRVIDEOINFODTO_TYPE_TOKEN);
    }

    return Optional.empty();
  }
}
