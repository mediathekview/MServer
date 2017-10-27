package de.mediathekview.mserver.crawler.dw.tasks;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.DwCrawler;

public class DWUebersichtTask extends AbstractDocumentTask<URL, CrawlerUrlDTO> {
  private static final String ADD_PAGE_NUMBER = ".addPage .number";
  private static final long serialVersionUID = 2080583393530906001L;
  private static final Logger LOG = LogManager.getLogger(DWUebersichtTask.class);
  private static final String SENDUNG_LINK_SELEKTOR = ".mcProgramsTeaser .smallList li:eq(1) a";
  private static final String RESULTS_COUNT_REGEX_PATTERN = "(?<=results=)\\d+";

  public DWUebersichtTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private String addBaseParameters(final String aUrl) {
    String newUrl;
    newUrl = UrlUtils.changeOrAddParameter(aUrl, "filter", "");
    newUrl = UrlUtils.changeOrAddParameter(newUrl, "type", "18");
    newUrl = UrlUtils.changeOrAddParameter(newUrl, "sort", "date");
    return newUrl;
  }

  private Optional<AbstractUrlTask<URL, CrawlerUrlDTO>> createNextPageCrawler(
      final CrawlerUrlDTO aUrlDTO) {
    final Optional<Integer> resultsCount = gatherResultsCount(aUrlDTO);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageUrls = new ConcurrentLinkedQueue<>();
    nextPageUrls
        .offer(new CrawlerUrlDTO(UrlUtils.changeOrAddParameter(addBaseParameters(aUrlDTO.getUrl()),
            "results", resultsCount.orElse(0).toString())));
    return Optional.of(createNewOwnInstance(nextPageUrls));
  }

  private Optional<Integer> gatherResultsCount(final CrawlerUrlDTO aUrlDTO) {
    final Matcher resultsRegexMatcher =
        Pattern.compile(RESULTS_COUNT_REGEX_PATTERN).matcher(aUrlDTO.getUrl());
    if (resultsRegexMatcher.find()) {
      try {
        return Optional.of(Integer.parseInt(resultsRegexMatcher.group()));
      } catch (final NumberFormatException numberFormatException) {
        LOG.debug("Something wen't teribble wrong on gathering the results count for DW.",
            numberFormatException);
      }
    }
    return Optional.empty();
  }

  @Override
  protected AbstractUrlTask<URL, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DWUebersichtTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    try {
      final Elements foundLinks = aDocument.select(SENDUNG_LINK_SELEKTOR);
      final Elements foundNextSiteLink = aDocument.select(ADD_PAGE_NUMBER);

      Optional<AbstractUrlTask<URL, CrawlerUrlDTO>> nextPageTask;
      if (foundNextSiteLink.isEmpty()) {
        nextPageTask = Optional.empty();
      } else {
        nextPageTask = createNextPageCrawler(aUrlDTO);
        if (nextPageTask.isPresent()) {
          nextPageTask.get().fork();
        }
      }


      for (final Element link : foundLinks) {
        if (link.hasAttr(Consts.ATTRIBUTE_HREF)) {
          taskResults.add(new URL(DwCrawler.BASE_URL + link.attr(Consts.ATTRIBUTE_HREF)));
          crawler.incrementAndGetMaxCount();
          crawler.updateProgress();
        }
      }

      if (nextPageTask.isPresent()) {
        taskResults.addAll(nextPageTask.get().join());
      }
    } catch (final IOException ioException) {
      LOG.fatal("Something wen't terrible wrong on getting the Sendung Verpasst for DW.",
          ioException);
    }
  }

}
