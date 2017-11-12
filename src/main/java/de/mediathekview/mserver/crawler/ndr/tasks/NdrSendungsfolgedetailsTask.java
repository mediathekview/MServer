package de.mediathekview.mserver.crawler.ndr.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class NdrSendungsfolgedetailsTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(NdrSendungsfolgedetailsTask.class);

  public NdrSendungsfolgedetailsTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private LocalDateTime parseTime(final String aText) {
    // Parse dates like: 12.11.2017 23:15 Uhr
    return LocalDateTime.parse(aText, DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm Uhr"));
  }

  @Override
  protected AbstractUrlTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new NdrSendungsfolgedetailsTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {

    final String titel = aDocument.select(".textinfo h1").first().text();
    final String thema = aDocument.select(".textinfo .subline span:eq(1)").first().text();
    final LocalDateTime time = parseTime(aDocument.select(".textinfo .subline span:eq(2)").text());
    // TODO nicklas2751: Implement me
    // final Duration dauer;
    // final Film newFilm =
    // new Film(UUID.randomUUID(), crawler.getSender(), titel, thema, time, dauer);
    // try {
    // newFilm.setWebsite(new URL(aUrlDTO.getUrl()));
    // } catch (final MalformedURLException malformedURLException) {
    // // I don't know why how and when this can happen but you know.
    // LOG.fatal("Something went terrible wrong on converting the actual website url to a url.",
    // malformedURLException);
    // }
    // taskResults.add(newFilm);
  }

}
