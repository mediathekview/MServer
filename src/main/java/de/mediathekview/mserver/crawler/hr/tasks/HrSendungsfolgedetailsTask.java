package de.mediathekview.mserver.crawler.hr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.utils.GeoLocationGuesser;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.parser.HrVideoJsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class HrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_VIDEO_JSON = "data-hr-mediaplayer-loader";
  private static final Logger LOG = LogManager.getLogger(HrSendungsfolgedetailsTask.class);
  private static final long serialVersionUID = 6138774185290017974L;
  private static final String THEMA_SELECTOR1 = ".breadcrumbNav__item span[itemprop=title]";
  private static final String THEMA_SELECTOR2 = ".c-programHeader__headline.text__headline";
  private static final String TITLE_SELECTOR1 = ".c-programHeader__subline.text__topline";
  private static final String TITLE_SELECTOR2 = ".c-contentHeader__headline";
  private static final String DATE_TIME_SELECTOR1 = ".c-programHeader__mediaWrapper time";
  private static final String DATE_TIME_SELECTOR2 = ".c-contentHeader__lead time";
  private static final String DAUER_SELECTOR1 = ".c-programHeader__mediaWrapper .mediaInfo__byline";
  private static final String DAUER_SELECTOR2 = ".c-contentHeader .mediaInfo__byline";
  private static final String DESCRIPTION_SELECTOR1 = ".copytext__text.copytext__text strong";
  private static final String DESCRIPTION_SELECTOR2 = ".copytext__text";
  private static final String VIDEO_URL_SELECTOR2 = "figure .js-loadScript";

  private static final Type OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN =
      new TypeToken<Optional<ArdVideoInfoDto>>() {}.getType();

  private final Gson gson;

  public HrSendungsfolgedetailsTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, urlToCrawlDTOs, jsoupConnection);

    gson =
        new GsonBuilder()
            .registerTypeAdapter(OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN, new HrVideoJsonDeserializer())
            .create();
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new HrSendungsfolgedetailsTask(crawler, aUrlsToCrawl, getJsoupConnection());
  }

  private static HrTopicTitleDTO getTopicAndTitle(final Document aDocument) {

    // Komplizierte Logik, da der HR nicht eindeutig das Thema und den Titel setzt
    final Elements breadcrumbElements = aDocument.select(THEMA_SELECTOR1);
    Optional<String> topic;
    Optional<String> title;

    if (breadcrumbElements.size() > 2) {
      // Bei den meisten Seiten lässt sich das Thema aus der Breadcrumb auslesen
      // Der Titel ist dann mal die Überschrift mal die Unterüberschrift
      topic = Optional.ofNullable(breadcrumbElements.get(2).text());
      title = HtmlDocumentUtils.getElementString(THEMA_SELECTOR2, aDocument);
      if (title.isPresent()
          && topic.isPresent()
          && topic.get().compareToIgnoreCase(title.get()) == 0) {
        title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR1, TITLE_SELECTOR2, aDocument);
      }
    } else {
      // Ansonsten ist die Überschrift das Thema und die Unterüberschrift der Titel (betrifft v.a.
      // Hessenschau)
      topic = HtmlDocumentUtils.getElementString(THEMA_SELECTOR2, aDocument);
      title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR1, TITLE_SELECTOR2, aDocument);
      if (topic.isEmpty() && title.isPresent()) {
        final String[] titleParts = title.get().split("-");
        topic = Optional.of(titleParts[0].trim());
      }
    }

    return new HrTopicTitleDTO(topic.orElse(null), title.orElse(null));
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    final Optional<String> dateTimeText =
        HtmlDocumentUtils.getElementAttributeString(
            DATE_TIME_SELECTOR1, DATE_TIME_SELECTOR2, ATTRIBUTE_DATETIME, aDocument);

    if (dateTimeText.isPresent()) {
      final String fixedDateTimeText =
          DateUtils.changeDateTimeForMissingISO8601Support(dateTimeText.get());
      try {
        return Optional.of(LocalDateTime.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE_TIME));
      } catch (final DateTimeParseException dateTimeParseException) {
        try {
          return Optional.of(
              LocalDate.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } catch (final DateTimeParseException dateTimeParseException2) {
          LOG.error(
              String.format(
                  "Can't parse either a date time nor only a date for HR: \"%s\"",
                  fixedDateTimeText),
              dateTimeParseException2);
        }
      }
    }
    return Optional.empty();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO urlDto, final Document aDocument) {
    final HrTopicTitleDTO topicAndTitle = getTopicAndTitle(aDocument);
    final Optional<String> beschreibung =
        HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR1, DESCRIPTION_SELECTOR2, aDocument);
    final Optional<ArdVideoInfoDto> ardVideoInfoDto = parseVideo(aDocument);
    final Optional<Duration> dauer = parseDauer(aDocument);

    if (topicAndTitle.getTitle().isEmpty()) {
      crawler.printMissingElementErrorMessage(urlDto.getUrl() + ": Titel");
      return;
    }

    if (topicAndTitle.getTopic().isEmpty()) {
      crawler.printMissingElementErrorMessage(urlDto.getUrl() + ": Thema");
      return;
    }

    if (ardVideoInfoDto.isEmpty() || ardVideoInfoDto.get().getDefaultVideoUrl().isEmpty()) {
      crawler.printMissingElementErrorMessage(urlDto.getUrl() + ": Video-Url");
      return;
    }

    if (dauer.isEmpty()) {
      crawler.printMissingElementErrorMessage(urlDto.getUrl() + ": Dauer");
      return;
    }

    final Optional<LocalDateTime> time = parseDate(aDocument);

    createFilm(
        urlDto,
        topicAndTitle.getTopic().get(),
        topicAndTitle.getTitle().get(),
        beschreibung.orElse(null),
        ardVideoInfoDto.get().getVideoUrls(),
        ardVideoInfoDto.get().getSubtitleUrlOptional().orElse(null),
        dauer.get(),
        time.orElse(null));
  }

  private void createFilm(
      final CrawlerUrlDTO aUrlDto,
      final String topic,
      final String title,
      @Nullable final String beschreibung,
      final Map<Resolution, String> videoUrls,
      @Nullable final String untertitelUrlText,
      final Duration dauer,
      @Nullable final LocalDateTime time) {
    try {
      final Film newFilm =
          new Film(
              UUID.randomUUID(),
              crawler.getSender(),
              title,
              topic,
              Optional.ofNullable(time).orElse(LocalDate.now().atStartOfDay()),
              dauer);
      newFilm.setWebsite(new URL(aUrlDto.getUrl()));
      Optional.ofNullable(beschreibung).ifPresent(newFilm::setBeschreibung);

      for (final Entry<Resolution, String> videoUrl : videoUrls.entrySet()) {
        newFilm.addUrl(videoUrl.getKey(), new FilmUrl(videoUrl.getValue(), serialVersionUID));
      }

      final Optional<String> optionalUntertitelUrlText = Optional.ofNullable(untertitelUrlText);
      if (optionalUntertitelUrlText.isPresent()) {
        newFilm.addSubtitle(new URL(optionalUntertitelUrlText.get()));
      }

      final Optional<FilmUrl> defaultUrl = newFilm.getDefaultUrl();
      defaultUrl.ifPresent(
          filmUrl ->
              newFilm.setGeoLocations(
                  GeoLocationGuesser.getGeoLocations(
                      crawler.getSender(), filmUrl.getUrl().toString())));

      taskResults.add(newFilm);
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    } catch (final MalformedURLException malformedUrlException) {
      LOG.fatal("A HR URL can't be parsed.", malformedUrlException);
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  private static Optional<Duration> parseDauer(final Document aDocument) {
    final Optional<String> dauerText =
        HtmlDocumentUtils.getElementString(DAUER_SELECTOR1, DAUER_SELECTOR2, aDocument);
    if (dauerText.isPresent()) {
      return HtmlDocumentUtils.parseDuration(dauerText);
    } else {
      return Optional.empty();
    }
  }

  private Optional<ArdVideoInfoDto> parseVideo(final Document aDocument) {

    final Optional<String> videoJson =
        HtmlDocumentUtils.getElementAttributeString(
            VIDEO_URL_SELECTOR2, ATTRIBUTE_VIDEO_JSON, aDocument);
    if (videoJson.isPresent()) {
      return gson.fromJson(videoJson.get(), OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN);
    }

    return Optional.empty();
  }
}
