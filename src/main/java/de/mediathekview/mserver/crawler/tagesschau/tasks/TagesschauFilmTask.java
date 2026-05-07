package de.mediathekview.mserver.crawler.tagesschau.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.FilmUrl;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.UUID;

/**
 * Task for processing Tagesschau archive pages.
 * Extracts links to daily broadcasts and creates Film objects.
 */
public class TagesschauFilmTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(TagesschauFilmTask.class);
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

  public TagesschauFilmTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrls) {
    super(aCrawler, aUrls);
  }

  @Override
  protected void processDocument(
      final CrawlerUrlDTO aUrlDTO,
      final Document aDocument) {
    try {
      LOG.debug("Processing Tagesschau archive page: {}", aUrlDTO.getUrl());

      // Look for video/broadcast links on the page
      Elements teaserLinks = aDocument.select("a[href*='/multimedia/sendung/ts/vor20jahren']");

      for (Element link : teaserLinks) {
        try {
          String href = link.attr("href");
          String title = link.select(".teaser-absatz__headline").text();
          String description = link.select(".teaser-absatz__shorttext").text();

          if (!href.startsWith("/")) {
            continue;
          }

          // Make absolute URL
          String fullUrl = "https://www.tagesschau.de" + href;

          // Try to extract date from URL
          String dateStr = extractDateFromUrl(href);

          if (!title.isEmpty()) {
            Film film = createFilmFromTeaser(title, description, dateStr, fullUrl);
            if (film != null) {
              taskResults.add(film);
              crawler.incrementAndGetActualCount();
            }
          }

        } catch (final Exception e) {
          LOG.debug("Error parsing teaser link", e);
          crawler.incrementAndGetErrorCount();
        }
      }

      // If we found links, we're done
      if (!taskResults.isEmpty()) {
        LOG.debug("Found {} films on archive page", taskResults.size());
        return;
      }

      // Otherwise, try to extract from video elements
      Elements videoElements = aDocument.select("[data-js_component='video'], video, .video");

      for (Element videoElem : videoElements) {
        try {
          Film film = parseVideoElement(videoElem);
          if (film != null && !film.getUrls().isEmpty()) {
            taskResults.add(film);
            crawler.incrementAndGetActualCount();
          }
        } catch (final Exception e) {
          LOG.debug("Error parsing video element", e);
          crawler.incrementAndGetErrorCount();
        }
      }

    } catch (final Exception e) {
      crawler.incrementAndGetErrorCount();
      LOG.error("Error processing document: {}", aUrlDTO.getUrl(), e);
    }
  }

  /**
   * Creates a Film object from teaser information.
   */
  private Film createFilmFromTeaser(String title, String description, String dateStr, String url) {
    try {
      Film film = new Film(
          UUID.randomUUID(),
          crawler.getSender(),
          title.trim(),
          "Tagesschau vor 20 Jahren",
          dateStr != null ? LocalDate.parse(dateStr, DATE_FORMAT).atStartOfDay() : null,
          null);

      if (!description.isEmpty()) {
        film.setBeschreibung(description.trim());
      }

      // Add URL
      try {
        FilmUrl filmUrl = new FilmUrl(url, 0L);
        film.addUrl(Resolution.HD, filmUrl);
      } catch (final MalformedURLException e) {
        LOG.warn("Invalid URL: {}", url, e);
        return null;
      }

      return film;

    } catch (final Exception e) {
      LOG.debug("Error creating film from teaser", e);
      return null;
    }
  }

  /**
   * Parses a video element to extract Film information.
   */
  private Film parseVideoElement(Element videoElem) {
    try {
      String title = "";
      Element titleElem = videoElem.selectFirst(".video-title, .headline, h3, h2");
      if (titleElem != null) {
        title = titleElem.text();
      }

      if (title.isEmpty()) {
        title = "Tagesschau Archiv";
      }

      Film film = new Film(
          UUID.randomUUID(),
          crawler.getSender(),
          title,
          "Tagesschau vor 20 Jahren",
          null,
          null);

      // Try to get description
      Element descElem = videoElem.selectFirst(".description, .shorttext, p");
      if (descElem != null) {
        String desc = descElem.text();
        if (!desc.isEmpty()) {
          film.setBeschreibung(desc);
        }
      }

      // Try to get URL
      String url = videoElem.attr("data-href");
      if (url.isEmpty()) {
        Element linkElem = videoElem.selectFirst("a[href]");
        if (linkElem != null) {
          url = linkElem.attr("href");
        }
      }
      if (url.isEmpty()) {
        Element sourceElem = videoElem.selectFirst("source");
        if (sourceElem != null) {
          url = sourceElem.attr("src");
        }
      }

      if (!url.isEmpty()) {
        if (!url.startsWith("http")) {
          url = "https://www.tagesschau.de" + (url.startsWith("/") ? "" : "/") + url;
        }
        try {
          FilmUrl filmUrl = new FilmUrl(url, 0L);
          film.addUrl(Resolution.HD, filmUrl);
        } catch (final MalformedURLException e) {
          LOG.warn("Invalid URL: {}", url, e);
          return null;
        }
      }

      return film;

    } catch (final Exception e) {
      LOG.debug("Error parsing video element", e);
      return null;
    }
  }

  /**
   * Extracts date from URL in format yyyyMMdd.
   * Example: ts-vor20jahren-20060401 -> 20060401
   */
  private String extractDateFromUrl(String url) {
    try {
      // Look for pattern like vor20jahren-20060401
      if (url.contains("vor20jahren-")) {
        String[] parts = url.split("vor20jahren-");
        if (parts.length > 1) {
          // Extract date part (should be 8 digits)
          String datepart = parts[1].replaceAll("\\D", "");
          if (datepart.length() >= 8) {
            return datepart.substring(0, 8);
          }
        }
      }
    } catch (final Exception e) {
      LOG.debug("Could not extract date from URL: {}", url);
    }
    return null;
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauFilmTask(crawler, aElementsToProcess);
  }
}



