package de.mediathekview.mserver.crawler.tagesschau.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Queue;

/**
 * Overview task for Tagesschau archive pages.
 * Reads an overview page and extracts URLs to daily archive pages (as CrawlerUrlDTO).
 */
public class TagesschauOverviewTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(TagesschauOverviewTask.class);

  private static final String[] BLACKLIST = new String[] {
          TagesschauConstants.ARCHIVE_START_URL
  };

  public TagesschauOverviewTask(final AbstractCrawler aCrawler, final Queue<CrawlerUrlDTO> aUrls) {
    super(aCrawler, aUrls);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    LOG.debug("Processing Tagesschau overview page: {}", aUrlDTO.getUrl());

    // Find links that reference the "vor20jahren" archives. The page contains two
    // kinds of URLs for year/overview pages, e.g.:
    // - /multimedia/tsvorzwanzigjahren-472.html
    // - /inland/tsvorzwanzigjahren-ts-100.html
    final Elements links = aDocument.select("a[href*='tsvorzwanzigjahren']");

    // Pattern to validate and capture the numeric id or year slug at the end
    final Pattern p = Pattern.compile(".*/(tsvorzwanzigjahren(?:-ts)?-?\\d+)\\.html$");

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
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauOverviewTask(crawler, aElementsToProcess);
  }
}




