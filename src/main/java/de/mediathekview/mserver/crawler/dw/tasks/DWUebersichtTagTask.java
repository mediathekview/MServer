package de.mediathekview.mserver.crawler.dw.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.DwCrawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.URL;
import java.util.Queue;

public class DWUebersichtTagTask extends AbstractDocumentTask<URL, CrawlerUrlDTO> {
  private static final long serialVersionUID = 2080583393530906001L;
  private static final Logger LOG = LogManager.getLogger(DWUebersichtTagTask.class);
  private static final String SENDUNG_LINK_SELEKTOR = ".mcProgramsTeaser .smallList li:eq(1) a";
  private static final String SENDUNG_LINK_SELEKTOR2 = ".searchres a";

  public DWUebersichtTagTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(aCrawler, urlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<URL, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aURLsToCrawl) {
    return new DWUebersichtTagTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    try {
      final Elements foundLinks = aDocument.select(SENDUNG_LINK_SELEKTOR);
      if (foundLinks.isEmpty()) {
        foundLinks.addAll(aDocument.select(SENDUNG_LINK_SELEKTOR2));
      }
      if (foundLinks.size() > 499) {
        LOG.error("DW MaxPageSize reached - change settings " + aUrlDTO.getUrl());
      }
      for (final Element link : foundLinks) {
        if (link.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
          taskResults.add(new URL(DwCrawler.BASE_URL + link.attr(HtmlConsts.ATTRIBUTE_HREF)));
          crawler.incrementAndGetMaxCount();
          crawler.updateProgress();
        }
      }
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something went terrible wrong on getting the Sendung Verpasst for DW.", ioException);
    }
  }
}
