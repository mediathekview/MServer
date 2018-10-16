package de.mediathekview.mserver.crawler.hr.tasks;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_SRC;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDTO;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.parser.HrVideoJsonDeserializer;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.CrawlerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class HrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final String ATTRIBUTE_DATETIME = "datetime";
  private static final String ATTRIBUTE_VIDEO_JSON = "data-hr-video-adaptive";
  private static final Logger LOG = LogManager.getLogger(HrSendungsfolgedetailsTask.class);
  private static final long serialVersionUID = 6138774185290017974L;
  private static final String THEMA_SELECTOR = ".c-programHeader__headline.text__headline";
  private static final String TITLE_SELECTOR1 = ".c-programHeader__subline.text__topline";
  private static final String TITLE_SELECTOR2 = ".c-contentHeader__headline";
  private static final String DATE_TIME_SELECTOR1 = ".c-programHeader__mediaWrapper time";
  private static final String DATE_TIME_SELECTOR2 = ".c-contentHeader__lead time";
  private static final String DAUER_SELECTOR1 = ".c-programHeader__mediaWrapper .mediaInfo__byline";
  private static final String DAUER_SELECTOR2 = ".c-contentHeader .mediaInfo__byline";
  private static final String DESCRIPTION_SELECTOR1 = ".copytext__text.copytext__text strong";
  private static final String DESCRIPTION_SELECTOR2 = ".copytext__text";
  private static final String VIDEO_URL_SELECTOR1 = ".c-programHeader__mediaWrapper source";
  private static final String VIDEO_URL_SELECTOR2 = "div.videoElement";
  private static final String UT_URL_SELECTOR1 = ".c-programHeader__mediaWrapper track";
  private static final String UT_URL_SELECTOR2 = ".c-contentHeader__lead track";

  private static final Type OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN = new TypeToken<Optional<ArdVideoInfoDTO>>() {
  }.getType();

  private final Gson gson;

  public HrSendungsfolgedetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);

    gson = new GsonBuilder()
        .registerTypeAdapter(OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN, new HrVideoJsonDeserializer())
        .create();
  }

  private Optional<LocalDateTime> parseDate(final Optional<String> aDateTimeText) {
    if (aDateTimeText.isPresent()) {
      final String fixedDateTimeText =
          DateUtils.changeDateTimeForMissingISO8601Support(aDateTimeText.get());
      try {
        return Optional.of(LocalDateTime.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE_TIME));
      } catch (final DateTimeParseException dateTimeParseException) {
        try {
          return Optional
              .of(LocalDate.parse(fixedDateTimeText, DateTimeFormatter.ISO_DATE).atStartOfDay());
        } catch (final DateTimeParseException dateTimeParseException2) {
          LOG.error(String.format("Can't parse either a date time nor only a date for HR: \"%s\"",
              fixedDateTimeText), dateTimeParseException2);
        }
      }
    }
    return Optional.empty();
  }


  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new HrSendungsfolgedetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Optional<String> titel = HtmlDocumentUtils.getElementString(TITLE_SELECTOR1, TITLE_SELECTOR2, aDocument);
    final Optional<String> thema = getTopic(aDocument, titel);
    final Optional<String> beschreibung =
        HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR1, DESCRIPTION_SELECTOR2, aDocument);
    final Map<Resolution, String> videoUrls = parseVideo(aDocument);
    final Optional<String> untertitelUrlText =
        HtmlDocumentUtils.getElementAttributeString(UT_URL_SELECTOR1, UT_URL_SELECTOR2, ATTRIBUTE_SRC, aDocument);
    final Optional<String> dateTimeText = HtmlDocumentUtils
        .getElementAttributeString(DATE_TIME_SELECTOR1, DATE_TIME_SELECTOR2, ATTRIBUTE_DATETIME, aDocument);
    final Optional<String> dauerText =
        HtmlDocumentUtils.getElementString(DAUER_SELECTOR1, DAUER_SELECTOR2, aDocument);
    final Optional<Duration> dauer;
    if (dauerText.isPresent()) {
      dauer = HtmlDocumentUtils.parseDuration(dauerText);
    } else {
      dauer = Optional.empty();
    }

    if (titel.isPresent()) {
      if (thema.isPresent()) {
        if (!videoUrls.isEmpty()) {
          if (dauer.isPresent()) {
            final Optional<LocalDateTime> time = parseDate(dateTimeText);

            try {
              final Film newFilm = new Film(UUID.randomUUID(), crawler.getSender(), titel.get(),
                  thema.get(), time.orElse(LocalDate.now().atStartOfDay()), dauer.get());
              newFilm.setWebsite(new URL(aUrlDTO.getUrl()));
              if (beschreibung.isPresent()) {
                newFilm.setBeschreibung(beschreibung.get());
              }

              for (Entry<Resolution, String> videoUrl : videoUrls.entrySet()) {
                newFilm.addUrl(videoUrl.getKey(),
                    CrawlerTool.uriToFilmUrl(new URL(videoUrl.getValue())));
              }

              if (untertitelUrlText.isPresent()) {
                newFilm.addSubtitle(new URL(untertitelUrlText.get()));
              }

              final Optional<FilmUrl> defaultUrl = newFilm.getDefaultUrl();
              if (defaultUrl.isPresent()) {
                newFilm.setGeoLocations(CrawlerTool.getGeoLocations(crawler.getSender(),
                    defaultUrl.get().getUrl().toString()));
              }

              taskResults.add(newFilm);
              crawler.incrementAndGetActualCount();
              crawler.updateProgress();
            } catch (final MalformedURLException malformedURLException) {
              LOG.fatal("A HR URL can't be parsed.", malformedURLException);
              crawler.printErrorMessage();
              crawler.incrementAndGetErrorCount();
              crawler.updateProgress();
            }
          } else {
            crawler.printMissingElementErrorMessage(aUrlDTO.getUrl() + ": Dauer");
          }
        } else {
          crawler.printMissingElementErrorMessage(aUrlDTO.getUrl() + ": Video-Url");
        }
      } else {
        crawler.printMissingElementErrorMessage(aUrlDTO.getUrl() + ": Titel");
      }
    } else {
      crawler.printMissingElementErrorMessage(aUrlDTO.getUrl() + ": Thema");
    }
  }

  private Optional<String> getTopic(final Document aDocument, final Optional<String> aTitle) {
    Optional<String> topic = HtmlDocumentUtils.getElementString(THEMA_SELECTOR, aDocument);
    if (!topic.isPresent() && aTitle.isPresent()) {
      String[] titleParts = aTitle.get().split("-");
      topic = Optional.of(titleParts[0].trim());
    }

    return topic;
  }

  private Map<Resolution, String> parseVideo(Document aDocument) {

    Map<Resolution, String> urls = new EnumMap<>(Resolution.class);

    // old video url
    Optional<String> videoUrl = HtmlDocumentUtils.getElementAttributeString(VIDEO_URL_SELECTOR1, ATTRIBUTE_SRC, aDocument);
    if (videoUrl.isPresent()) {
      urls.put(Resolution.NORMAL, videoUrl.get());
    } else {
      Optional<String> videoJson = HtmlDocumentUtils.getElementAttributeString(VIDEO_URL_SELECTOR2, ATTRIBUTE_VIDEO_JSON, aDocument);
      if (videoJson.isPresent()) {
        Optional<ArdVideoInfoDTO> dto = gson.fromJson(videoJson.get(), OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN);
        if (dto.isPresent()) {
          urls.putAll(dto.get().getVideoUrls());
        }
      }
    }

    return urls;
  }
}
