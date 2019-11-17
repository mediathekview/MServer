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
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

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
            new TypeToken<Optional<ArdVideoInfoDto>>() {
            }.getType();

    private final Gson gson;

    public HrSendungsfolgedetailsTask(
            final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
        super(aCrawler, aUrlToCrawlDtos);

        gson =
                new GsonBuilder()
                        .registerTypeAdapter(OPTIONAL_ARDVIDEOINFODTO_TYPE_TOKEN, new HrVideoJsonDeserializer())
                        .create();
    }

    @Override
    protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
            final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
        return new HrSendungsfolgedetailsTask(crawler, aUrlsToCrawl);
    }

    private static Optional<String>[] getTopicAndTitle(final Document aDocument) {
        final Optional<String>[] topicAndTitle = new Optional[2];

        // Komplizierte Logik, da der HR nicht eindeutig das Thema und den Titel setzt
        final Elements breadcrumbElements = aDocument.select(THEMA_SELECTOR1);
        if (breadcrumbElements.size() > 2) {
            // Bei den meisten Seiten lässt sich das Thema aus der Breadcrumb auslesen
            // Der Titel ist dann mal die Überschrift mal die Unterüberschrift
            topicAndTitle[0] = Optional.of(breadcrumbElements.get(2).text());
            final Optional<String> title = HtmlDocumentUtils.getElementString(THEMA_SELECTOR2, aDocument);
            if (title.isPresent()
                    && topicAndTitle[0].isPresent()
                    && topicAndTitle[0].get().compareToIgnoreCase(title.get()) == 0) {
                topicAndTitle[1] =
                        HtmlDocumentUtils.getElementString(TITLE_SELECTOR1, TITLE_SELECTOR2, aDocument);
            } else {
                topicAndTitle[1] = title;
            }
        } else {
            // Ansonsten ist die Überschrift das Thema und die Unterüberschrift der Titel (betrifft v.a.
            // Hessenschau)
            topicAndTitle[0] = HtmlDocumentUtils.getElementString(THEMA_SELECTOR2, aDocument);
            topicAndTitle[1] =
                    HtmlDocumentUtils.getElementString(TITLE_SELECTOR1, TITLE_SELECTOR2, aDocument);
            if (!topicAndTitle[0].isPresent() && topicAndTitle[1].isPresent()) {
                final String[] titleParts = topicAndTitle[1].get().split("-");
                topicAndTitle[0] = Optional.of(titleParts[0].trim());
            }
        }

        return topicAndTitle;
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
    protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
        final Optional<String>[] topicAndTitle = getTopicAndTitle(aDocument);
        final Optional<String> beschreibung =
                HtmlDocumentUtils.getElementString(DESCRIPTION_SELECTOR1, DESCRIPTION_SELECTOR2, aDocument);
        final Optional<ArdVideoInfoDto> ardVideoInfoDto = parseVideo(aDocument);
        final Optional<Duration> dauer = parseDauer(aDocument);

        if (checkPreConditions(topicAndTitle, ardVideoInfoDto, dauer, aUrlDto.getUrl())) {

            final Optional<LocalDateTime> time = parseDate(aDocument);

            createFilm(
                    aUrlDto,
                    topicAndTitle[0].get(),
                    topicAndTitle[1].get(),
                    beschreibung,
                    ardVideoInfoDto.get().getVideoUrls(),
                    ardVideoInfoDto.get().getSubtitleUrlOptional(),
                    dauer.get(),
                    time);
        }
    }

    private void createFilm(
            final CrawlerUrlDTO aUrlDto,
            final String topic,
            final String title,
            final Optional<String> beschreibung,
            final Map<Resolution, String> videoUrls,
            final Optional<String> untertitelUrlText,
            final Duration dauer,
            final Optional<LocalDateTime> time) {
        try {
            final Film newFilm =
                    new Film(
                            UUID.randomUUID(),
                            crawler.getSender(),
                            title,
                            topic,
                            time.orElse(LocalDate.now().atStartOfDay()),
                            dauer);
            newFilm.setWebsite(new URL(aUrlDto.getUrl()));
            beschreibung.ifPresent(newFilm::setBeschreibung);

            for (final Entry<Resolution, String> videoUrl : videoUrls.entrySet()) {
                newFilm.addUrl(videoUrl.getKey(), new FilmUrl(videoUrl.getValue(), serialVersionUID));
            }

            if (untertitelUrlText.isPresent()) {
                newFilm.addSubtitle(new URL(untertitelUrlText.get()));
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

    private boolean checkPreConditions(
            final Optional<String>[] topicAndTitle,
            final Optional<ArdVideoInfoDto> videoInfoDto,
            final Optional<Duration> dauer,
            final String aUrl) {
        if (!topicAndTitle[1].isPresent()) {
            crawler.printMissingElementErrorMessage(aUrl + ": Titel");
            return false;
        }

        if (!topicAndTitle[0].isPresent()) {
            crawler.printMissingElementErrorMessage(aUrl + ": Thema");
            return false;
        }

        if (!videoInfoDto.isPresent() || videoInfoDto.get().getDefaultVideoUrl().isEmpty()) {
            crawler.printMissingElementErrorMessage(aUrl + ": Video-Url");
            return false;
        }

        if (!dauer.isPresent()) {
            crawler.printMissingElementErrorMessage(aUrl + ": Dauer");
            return false;
        }

        return true;
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
