package de.mediathekview.mserver.crawler.tagesschau.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.EntryUrlDto;
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
import org.jspecify.annotations.NonNull;

public class TagesschauEntriesTask extends AbstractDocumentTask<EntryUrlDto, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(TagesschauEntriesTask.class);

  private static final Pattern PATTERN_VIDEO = Pattern.compile("/video-\\d+\\.html$");
  private static final Pattern PATTERN_SUB_PAGE = Pattern.compile("/(tsvorzwanzigjahren(?:-ts)?-?\\d+)\\.html$");
  private static final String[] BLACKLIST = new String[] {TagesschauConstants.ARCHIVE_START_URL};

  public TagesschauEntriesTask(final AbstractCrawler crawler, final Queue<CrawlerUrlDTO> queue) {
    super(crawler, queue);
  }

  @Override
  protected void processDocument(CrawlerUrlDTO aUrlDTO, Document aDocument) {
    EntryUrlDto result = new EntryUrlDto();
    LOG.debug("Processing Tagesschau overview page: {}", aUrlDTO.getUrl());

    final Elements links = aDocument.select(".teaser-absatz__link");

    for (final Element link : links) {
      try {
        final String href = link.attr("href");
        if (href.isEmpty()) {
          continue;
        }
        // normalize to absolute
        final String fullUrl = href.startsWith("http") ? href : buildUrl(href);

        if (Arrays.stream(BLACKLIST).noneMatch(fullUrl::equalsIgnoreCase)) {
          final Matcher matcherSubPage = PATTERN_SUB_PAGE.matcher(fullUrl);
          final Matcher matcherVideo = PATTERN_VIDEO.matcher(fullUrl);
          if (matcherSubPage.find()) {
            result.addSubPage(new CrawlerUrlDTO(fullUrl));
          } else if (matcherVideo.find()) {
            result.addVideo(new CrawlerUrlDTO(fullUrl));
          }
        }
      } catch (final Exception e) {
        LOG.error("Error while processing overview link", e);
      }
    }

    taskResults.add(result);
  }

  private static @NonNull String buildUrl(String href) {
    return "https://www.tagesschau.de" + (href.startsWith("/") ? "" : "/") + href;
  }

  @Override
  protected AbstractRecursiveConverterTask<EntryUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new TagesschauEntriesTask(crawler, aElementsToProcess);
  }
}
