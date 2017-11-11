package mServer.crawler.sender.wdr;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.CrawlerTool;

public class WdrVideoDetailsDeserializer extends HtmlDeserializerBase {

  private static final String QUERY_URL = "div.videoLink > a";
  private static final String META_ITEMPROP_DESCRIPTION = "description";
  private static final String META_ITEMPROP_TITLE = "name";
  private static final String META_ITEMPROP_WEBSITE = "url";
  private static final String META_PROPERTY_DATE = "dcterms.date";
  private static final String META_PROPERTY_DURATION = "video:duration";

  private static final String JSON_ELEMENT_MEDIAOBJ = "mediaObj";
  private static final String JSON_ATTRIBUTE_URL = "url";

  private static final Logger LOG = LogManager.getLogger(WdrVideoDetailsDeserializer.class);

  private final WdrVideoUrlParser videoUrlParser;
  private final DateTimeFormatter dateFormatDatenFilm = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private final DateTimeFormatter timeFormatDatenFilm = DateTimeFormatter.ofPattern("HH:mm:ss");

  public WdrVideoDetailsDeserializer(final WdrUrlLoader aUrlLoader) {
    videoUrlParser = new WdrVideoUrlParser(aUrlLoader);
  }

  public Film deserialize(String theme, final Document document) {

    String date = "";
    final String description =
        getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_DESCRIPTION);
    String time = "";
    final String title = getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_TITLE);
    final String website = getMetaValue(document, QUERY_META_ITEMPROP, META_ITEMPROP_WEBSITE);
    final String durationString =
        getMetaValue(document, QUERY_META_PROPERTY, META_PROPERTY_DURATION);
    long duration = 0;

    if (durationString != null && !durationString.isEmpty()) {
      duration = Long.parseLong(durationString);
    }

    final String dateTime = getMetaValue(document, QUERY_META_NAME, META_PROPERTY_DATE);
    if (!dateTime.isEmpty()) {
      try {
        final LocalDateTime d =
            LocalDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        date = d.format(dateFormatDatenFilm);
        time = d.format(timeFormatDatenFilm);
      } catch (final DateTimeParseException ex) {
        LOG.error(website, ex);
      }
    }

    final String jsUrl = getVideoJavaScriptUrl(document);
    if (jsUrl.isEmpty()) {
      return null;
    }

    final String t = getTheme(document, title);
    if (!t.isEmpty()) {
      theme = t;
    }

    final WdrVideoDto videoDto = videoUrlParser.parse(jsUrl);

    if (!videoDto.getUrl(Resolution.NORMAL).isEmpty()) {
      try {
        final Film film = CrawlerTool.createFilm(Sender.WDR, videoDto.getUrl(Resolution.NORMAL),
            title, theme, date, time, duration, website, description,
            videoDto.getUrl(Resolution.HD), videoDto.getUrl(Resolution.SMALL));

        if (!videoDto.getSubtitleUrl().isEmpty()) {
          film.addSubtitle(new URL(videoDto.getSubtitleUrl()));
        }

        return film;
      } catch (final MalformedURLException ex) {
        LOG.error(ex);
      }
    }

    return null;
  }

  private String getTheme(final Document document, final String title) {
    String theme = "";
    final Element titleElement = document.select("title").first();

    if (titleElement != null) {
      theme = titleElement.text().replace(" - Sendungen A-Z - Video - Mediathek - WDR", "")
          .replace("- Sendung - Video - Mediathek - WDR", "").replace(title, "");

      if (theme.startsWith("Video:")) {
        theme = theme.substring(6).trim();
      }
      if (theme.startsWith("- ")) {
        theme = theme.substring(2).trim();
      }
    }

    return theme;
  }

  private String getVideoJavaScriptUrl(final Document document) {
    // Die URL für das Video steht nicht direkt im HTML
    // stattdessen ist ein JavaScript eingebettet, dass die Video-Infos enthält

    String urlJs = "";

    final Element urlElement = document.select(QUERY_URL).first();
    if (urlElement != null) {
      final String extension = urlElement.attr("data-extension");

      final JsonParser jsonParser = new JsonParser();
      final JsonElement element = jsonParser.parse(extension);
      if (element != null && element.getAsJsonObject().has(JSON_ELEMENT_MEDIAOBJ)) {
        final JsonElement mediaObjElement = element.getAsJsonObject().get(JSON_ELEMENT_MEDIAOBJ);
        if (mediaObjElement != null && mediaObjElement.getAsJsonObject().has(JSON_ATTRIBUTE_URL)) {
          urlJs = mediaObjElement.getAsJsonObject().get(JSON_ATTRIBUTE_URL).getAsString();
        }
      }
    }

    return urlJs;
  }
}


