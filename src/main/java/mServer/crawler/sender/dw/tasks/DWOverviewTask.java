package mServer.crawler.sender.dw.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.dw.DWTaskBase;
import mServer.crawler.sender.dw.parser.DWSendungOverviewDeserializer;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DWOverviewTask extends DWTaskBase<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Type OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PagedElementListDTO<CrawlerUrlDTO>>>() {}.getType();
  private final int subpage;

  public DWOverviewTask(
      final MediathekReader crawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs,
      final int subpage)
  {
	  super(crawler, urlToCrawlDTOs, Optional.empty());
	  this.subpage = subpage;
	  registerJsonDeserializer(
	    OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN, new DWSendungOverviewDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PagedElementListDTO<CrawlerUrlDTO>> overviewDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_OVERVIEW_DTO_TYPE_TOKEN);
    if (!overviewDtoOptional.isPresent()) {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
      return;
    }

    final PagedElementListDTO<CrawlerUrlDTO> overviewDto = overviewDtoOptional.get();
    addResults(overviewDto.getElements());

    final Optional<String> optionalNextPage = overviewDto.getNextPage();
    if (optionalNextPage.isPresent() && subpage < getMaximumSubpages()) {
      final String nextPage = optionalNextPage.get();
      if (!aDTO.getUrl().endsWith(nextPage)) {
    	final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    	queue.add(new CrawlerUrlDTO(nextPage));
        taskResults.addAll(createNewOwnInstance(queue).invoke());
      }
    }
  }

  private int getMaximumSubpages() {
    if (CrawlerTool.loadShort()) {
      return 10;
    }
    return 100;
  }

  private void addResults(final Collection<CrawlerUrlDTO> aUrls) {
    for (final CrawlerUrlDTO url : aUrls) {
      taskResults.add(new CrawlerUrlDTO(url.getUrl()));
    }
  }

  @Override
  protected DWOverviewTask createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new DWOverviewTask(crawler, aElementsToProcess, subpage + 1);
  }

}
