package mServer.crawler.sender.wdr;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.Qualities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class WdrVideoDetailsDeserializer extends HtmlDeserializerBase {

  private static final String QUERY_URL = "div.videoLink > a";
  private static final String META_PROPERTY_DESCRIPTION = "og:description";
  private static final String META_PROPERTY_TITLE = "og:title";
  private static final String META_PROPERTY_WEBSITE = "og:url";
  private static final String META_PROPERTY_DATE = "dcterms.date";
  private static final String META_PROPERTY_DURATION = "video:duration";

  private static final String JSON_ELEMENT_MEDIAOBJ = "mediaObj";
  private static final String JSON_ATTRIBUTE_URL = "url";

  private static final Logger LOG = LogManager.getLogger(WdrVideoDetailsDeserializer.class);

  private final WdrVideoUrlParser videoUrlParser;
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  public WdrVideoDetailsDeserializer(WdrUrlLoader aUrlLoader) {
    videoUrlParser = new WdrVideoUrlParser(aUrlLoader);
  }

  public DatenFilm deserialize(String theme, Document document) {

    String date = "";
    String description = getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_DESCRIPTION);
    String time = "";
    String website = getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_WEBSITE);
    String durationString = getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_DURATION);
    long duration = 0;

    if (durationString != null && !durationString.isEmpty()) {
      duration = Long.parseLong(durationString);
    }

    String dateTime = getMetaValue(document, QUERY_META_NAME, META_PROPERTY_DATE);
    if (!dateTime.isEmpty()) {
      try {
        LocalDateTime d = LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        date = d.format(dateFormatDatenFilm);
        time = d.format(timeFormatDatenFilm);
      } catch (DateTimeParseException ex) {
        LOG.error(website, ex);
      }
    }

    String jsUrl = getVideoJavaScriptUrl(document);
    if (jsUrl.isEmpty()) {
      return null;
    }

    String t = getTheme(document, theme);
    if (!t.isEmpty()) {
      theme = t;
    }

    String title = getTitle(document, theme);

    WdrVideoDto videoDto = videoUrlParser.parse(jsUrl);

    if (!videoDto.getUrl(Qualities.NORMAL).isEmpty()) {
      DatenFilm film = new DatenFilm(Const.WDR, theme, website, title, videoDto.getUrl(Qualities.NORMAL), "", date, time, duration, description);

      if (!videoDto.getSubtitleUrl().isEmpty()) {
        CrawlerTool.addUrlSubtitle(film, videoDto.getSubtitleUrl());
      }
      if (!videoDto.getUrl(Qualities.SMALL).isEmpty()) {
        CrawlerTool.addUrlKlein(film, videoDto.getUrl(Qualities.SMALL), "");
      }
      if (!videoDto.getUrl(Qualities.HD).isEmpty()) {
        CrawlerTool.addUrlHd(film, videoDto.getUrl(Qualities.HD), "");
      }

      return film;
    }

    return null;
  }

  private String getTitle(Document document, String theme) {
    String title = getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_TITLE);
    if (title.startsWith(theme) && !title.equals(theme)) {
      title = title.replaceFirst(theme, "").trim();
      if (title.trim().startsWith("-")) {
        title = title.replaceFirst("-", "").trim();
      }
      if (title.trim().startsWith(":")) {
        title = title.replaceFirst(":", "").trim();
      }
    }

    return title;
  }

  private String getTheme(Document document, String actualTheme) {
    String theme = "";
    theme = getReducedTitleValue(document);

    int firstIndex = theme.indexOf(" - ");
    int lastIndex = theme.lastIndexOf(" - ");
    if (firstIndex > -1) {
      if (theme.substring(0, firstIndex).trim().equals(actualTheme)) {
        return actualTheme;
      }
      if (theme.substring(lastIndex + 3).trim().equals(actualTheme)) {
        return actualTheme;
      }
      theme = theme.substring(lastIndex + 3).trim();
    }

    return theme;
  }

  private String getReducedTitleValue(Document document) {
    String title = "";
    Element titleElement = document.select("title").first();

    if (titleElement != null) {
      title = titleElement.text()
              .replace(" - Sendungen A-Z - Video - Mediathek - WDR", "")
              .replace("- Sendung - Video - Mediathek - WDR", "")
              .replace("- Fernsehen - WDR", "")
              .replace(title, "");
      if (title.startsWith("Video:")) {
        title = title.substring(6).trim();
      }
      if (title.startsWith("- ")) {
        title = title.substring(2).trim();
      }
    }
    return title;
  }

  private String getVideoJavaScriptUrl(Document document) {
    // Die URL für das Video steht nicht direkt im HTML
    // stattdessen ist ein JavaScript eingebettet, dass die Video-Infos enthält

    String urlJs = "";

    Element urlElement = document.select(QUERY_URL).first();
    if (urlElement != null) {
      String extension = urlElement.attr("data-extension");

      JsonParser jsonParser = new JsonParser();
      JsonElement element = jsonParser.parse(extension);
      if (element != null && element.getAsJsonObject().has(JSON_ELEMENT_MEDIAOBJ)) {
        JsonElement mediaObjElement = element.getAsJsonObject().get(JSON_ELEMENT_MEDIAOBJ);
        if (mediaObjElement != null && mediaObjElement.getAsJsonObject().has(JSON_ATTRIBUTE_URL)) {
          urlJs = mediaObjElement.getAsJsonObject().get(JSON_ATTRIBUTE_URL).getAsString();
        }
      }
    }

    return urlJs;
  }
}
