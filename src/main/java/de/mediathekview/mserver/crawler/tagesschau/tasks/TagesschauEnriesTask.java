package de.mediathekview.mserver.crawler.tagesschau.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauConstants;

import java.util.Arrays;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TagesschauEnriesTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(TagesschauEnriesTask.class);

  private static final String[] BLACKLIST = new String[] {TagesschauConstants.ARCHIVE_START_URL};

  public TagesschauEnriesTask(final AbstractCrawler crawler, final Queue<CrawlerUrlDTO> queue) {
    super(crawler, queue);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    LOG.debug("Processing Tagesschau overview page: {}", aUrlDTO.getUrl());

    // Find links that reference the "vor20jahren" archives. The page contains two
    // kinds of URLs for year/overview pages, e.g.:
    // - /multimedia/tsvorzwanzigjahren-472.html
    // - /inland/tsvorzwanzigjahren-ts-100.html
    final Elements links = aDocument.select(".teaser-absatz__link");

    // Pattern to validate and capture the numeric id
    final Pattern p = Pattern.compile(".*/video-\\d+\\.html$");

    for (final Element link : links) {
      try {
        final String href = link.attr("href");
        if (href == null || href.isEmpty()) {
          continue;
        }
        // normalize to absolute
        final String fullUrl = href.startsWith("http") ? href : "https://www.tagesschau.de" + (href.startsWith("/") ? "" : "/") + href;

        final Matcher m = p.matcher(fullUrl);
        if (m.find() && Arrays.stream(BLACKLIST).noneMatch(fullUrl::equalsIgnoreCase)) {
          // Add the URL (deduplication is handled by the Set in taskResults)
          taskResults.add(new CrawlerUrlDTO(fullUrl));
          crawler.incrementAndGetActualCount();
        }
      } catch (final Exception e) {
        LOG.debug("Error while processing overview link", e);
        crawler.incrementAndGetErrorCount();
      }
    }

  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauEnriesTask(crawler, aElementsToProcess);
  }
}
