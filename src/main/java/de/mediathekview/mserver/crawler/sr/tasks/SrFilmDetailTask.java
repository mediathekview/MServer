package de.mediathekview.mserver.crawler.sr.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.utils.HtmlDocumentUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDto;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoJsonDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class SrFilmDetailTask extends AbstractDocumentTask<Film, SrTopicUrlDTO> {

  private static final org.apache.logging.log4j.Logger LOG =
      LogManager.getLogger(SrFilmDetailTask.class);

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMANY);
  private static final DateTimeFormatter DATE_TIME_FORMATTER_ENG =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.GERMANY);

  private static final String CONTENT_SELECTOR = "div.article__content";
  private static final String TITLE_SELECTOR = CONTENT_SELECTOR + " > div > h3";
  private static final String DETAILS_SELECTOR = CONTENT_SELECTOR + " > div > p";
  private static final String DESCRIPTION_SELECTOR = "h1.background-title";
  private static final String VIDEO_DETAIL_ATTRIBUTE = "data-mediacollection-ardplayer";
  private static final String VIDEO_DETAIL_SELECTOR = "div[" + VIDEO_DETAIL_ATTRIBUTE + "]";

  public SrFilmDetailTask(
      final AbstractCrawler crawler,
      final Queue<SrTopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
  }

  private static Optional<String> parseDescription(final Document aDocument) {
    final Elements elements = aDocument.select(DESCRIPTION_SELECTOR);
    if (elements.isEmpty()) {
      return Optional.empty();
    }
    final Node node = elements.first().nextSibling();
    return Optional.of(node.toString());
  }

  private static Optional<LocalDateTime> parseDate(final Document aDocument) {
    final Optional<String> date = getDetailElement(aDocument, 1);
    if (date.isPresent()) {
      final String isoDate = DateUtils.changeDateTimeForMissingISO8601Support(date.get().trim());
      try {
        final LocalDateTime localDate =
            LocalDateTime.parse(isoDate + " 00:00", DATE_TIME_FORMATTER);
        return Optional.of(localDate);
      } catch (final DateTimeParseException e) {
        try {
          final LocalDateTime localDate =
              LocalDateTime.parse(isoDate + " 00:00", DATE_TIME_FORMATTER_ENG);
          return Optional.of(localDate);
        } catch (final DateTimeParseException e1) {
          LOG.fatal(e1);
        }
      }
    }

    return Optional.empty();
  }

  private static Optional<Duration> parseDuration(final Document aDocument) {
    final Optional<String> duration = getDetailElement(aDocument, 2);
    if (duration.isPresent()) {
      final String[] parts = duration.get().replace("Dauer:", "").trim().split(":");
      if (parts.length == 3) {
        return Optional.of(
            Duration.ofHours(Long.parseLong(parts[0]))
                .plusMinutes(Long.parseLong(parts[1]))
                .plusSeconds(Long.parseLong(parts[2])));
      } else {
        LOG.debug("SrFilmDetailTask: unknown duration part count: {}", duration.get());
      }
    }

    return Optional.empty();
  }

  private static Optional<String> getDetailElement(final Document aDocument, final int index) {
    final Optional<String> details =
        HtmlDocumentUtils.getElementString(DETAILS_SELECTOR, aDocument);
    if (details.isPresent()) {
      final String[] parts = details.get().split("\\|");
      if (parts.length == 4) {
        return Optional.of(parts[index]);
      } else {
        LOG.debug("SrFilmDetailTask: unknown details part count: {}", details.get());
      }
    }

    return Optional.empty();
  }

  private static String addMissingProtocol(final String aUrl) {
    if (aUrl.startsWith("//")) {
      return "https:" + aUrl;
    }

    return aUrl;
  }

  @Override
  protected void processDocument(final SrTopicUrlDTO aUrlDTO, final Document aDocument) {

    try {
      if (!isRelevantType(aDocument)) {
        crawler.incrementAndGetActualCount();
        return;
      }

      final Optional<String> title = HtmlDocumentUtils.getElementString(TITLE_SELECTOR, aDocument);
      final Optional<LocalDateTime> time = parseDate(aDocument);
      final Optional<Duration> duration = parseDuration(aDocument);
      final Optional<String> description = parseDescription(aDocument);

      final Optional<ArdVideoInfoDto> videoInfoOptional = parseUrls(aDocument);
      if (videoInfoOptional.isPresent() && title.isPresent()) {
        final Film film =
            new Film(
                UUID.randomUUID(),
                crawler.getSender(),
                title.get(),
                aUrlDTO.getTheme(),
                time.orElse(LocalDateTime.now()),
                duration.orElse(Duration.ZERO));

        film.setWebsite(URI.create(aUrlDTO.getUrl()).toURL());
        description.ifPresent(film::setBeschreibung);

        final ArdVideoInfoDto videoInfo = videoInfoOptional.get();
        if (!videoInfo.getSubtitleUrl().isEmpty()) {
          for (String url : videoInfo.getSubtitleUrl()) {
            try {
              film.addSubtitle(URI.create(addMissingProtocol(url)).toURL());
            } catch (Exception e) {
              LOG.error("invalid subtitle url {} {}", url, e);
            }
          }
        }

        addUrls(film, videoInfo.getVideoUrls());

        if (taskResults.add(film)) {
          crawler.incrementAndGetActualCount();
        } else {
          crawler.incrementAndGetErrorCount();
          LOG.error("Rejected duplicate {}", film);
        }
        crawler.updateProgress();
      } else {
        LOG.error("SrFilmDetailTask: no title or video found for url {}", aUrlDTO.getUrl());
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    } catch (final MalformedURLException ex) {
      LOG.fatal("A SR URL can't be parsed.", ex);
      crawler.printErrorMessage();
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  @Override
  protected AbstractUrlTask<Film, SrTopicUrlDTO> createNewOwnInstance(
      final Queue<SrTopicUrlDTO> aURLsToCrawl) {
    return new SrFilmDetailTask(crawler, aURLsToCrawl);
  }

  /**
   * checks whether the multimedia type is relevant
   *
   * @param aDocument the html document
   * @return true if document is relevant
   */
  private boolean isRelevantType(final Document aDocument) {
    // ignore all documents who does not contain any video
    final Optional<String> type = getDetailElement(aDocument, 0);
    return type.isPresent() && type.get().toLowerCase().contains("video");
  }

  private void addUrls(final Film film, final Map<Resolution, String> videoUrls)
      throws MalformedURLException {

    for (final Map.Entry<Resolution, String> qualitiesEntry : videoUrls.entrySet()) {
      final String url = qualitiesEntry.getValue();
      film.addUrl(
          qualitiesEntry.getKey(),
          new FilmUrl(url, crawler.determineFileSizeInKB(url)));
    }
  }

  private Optional<ArdVideoInfoDto> parseUrls(final Document aDocument) {
    final Optional<String> videoDetailUrl =
        HtmlDocumentUtils.getElementAttributeString(
            VIDEO_DETAIL_SELECTOR, VIDEO_DETAIL_ATTRIBUTE, aDocument);
    if (videoDetailUrl.isPresent()) {
      final Gson gson =
          new GsonBuilder()
              .registerTypeAdapter(ArdVideoInfoDto.class, new ArdVideoInfoJsonDeserializer(crawler))
              .create();

      String url = videoDetailUrl.get();
      url = addMissingProtocol(url);

      try {
        final ArdVideoInfoDto dto =
            gson.fromJson(new InputStreamReader(URI.create(url).toURL().openStream()), ArdVideoInfoDto.class);
        if (dto.getVideoUrls().size() > 0) {
          return Optional.of(dto);
        }
      } catch (final Exception ex) {
        LOG.fatal(url, ex);
      }
    }

    return Optional.empty();
  }
}
