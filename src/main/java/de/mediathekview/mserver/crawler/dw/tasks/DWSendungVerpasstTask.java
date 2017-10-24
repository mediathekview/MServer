package de.mediathekview.mserver.crawler.dw.tasks;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class DWSendungVerpasstTask implements Callable<Set<URL>> {

  private static final Logger LOG = LogManager.getLogger(DWSendungVerpasstTask.class);
  private static final String BASE_URL = "http://www.dw.com/";
  private static final String SENDUNG_VERPASST_URL =
      BASE_URL + "de/media-center/sendung-verpasst/s-100815";
  private static final String SENDUNG_LINK_SELEKTOR = ".mcProgramsTeaser .smallList li:eq(1) a";
  private final AbstractCrawler crawler;

  public DWSendungVerpasstTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<URL> call() {
    final Set<URL> sendungFolgenUrls = new HashSet<>();

    try {
      final Document document = Jsoup.connect(SENDUNG_VERPASST_URL).get();
      final Elements foundLinks = document.select(SENDUNG_LINK_SELEKTOR);
      for (final Element link : foundLinks) {
        if (link.hasAttr(Consts.ATTRIBUTE_HREF)) {
          sendungFolgenUrls.add(new URL(BASE_URL + link.attr(Consts.ATTRIBUTE_HREF)));
          crawler.incrementAndGetMaxCount();
          crawler.updateProgress();
        }

      }
    } catch (final IOException ioException) {
      LOG.fatal("Something wen't terrible wrong on getting the Sendung Verpasst for DW.",
          ioException);
    }

    return sendungFolgenUrls;
  }

}
