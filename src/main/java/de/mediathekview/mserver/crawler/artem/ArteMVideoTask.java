package de.mediathekview.mserver.crawler.artem;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class ArteMVideoTask extends AbstractJsonRestTask<ArteMVideoDto, PagedElementListDTO<ArteMVideoDto>, CrawlerUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ArteMVideoTask.class);
  private int subPageIndex = 0;
  
  public ArteMVideoTask(AbstractCrawler crawler, Queue<CrawlerUrlDTO> urlToCrawlDTOs, String authKey, int subPageIndex) {
    super(crawler, urlToCrawlDTOs, authKey);
    this.subPageIndex = subPageIndex;
  }

  @Override
  protected Object getParser(CrawlerUrlDTO aDTO) {
    return new ArteMVideoDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<List<ArteMVideoDto>>() {}.getType();
  }

  @Override
  protected void handleHttpError(CrawlerUrlDTO dto, URI url, Response response) {
    crawler.printErrorMessage();
    LOG.fatal(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
  }

  @Override
  protected void postProcessing(PagedElementListDTO<ArteMVideoDto> aResponseObj, CrawlerUrlDTO aDTO) {
    final Optional<AbstractRecursiveConverterTask<ArteMVideoDto, CrawlerUrlDTO>> subpageCrawler;
    final Optional<String> nextPageLink = aResponseObj.getNextPage();
    if (nextPageLink.isPresent() && config.getMaximumSubpages() > subPageIndex) {
      final Queue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new CrawlerUrlDTO(nextPageLink.get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }
    for (ArteMVideoDto aFilm : aResponseObj.getElements()) {
      taskResults.add(aFilm);
    }
    //
    subpageCrawler.ifPresent(nextPageCrawler -> taskResults.addAll(nextPageCrawler.join()));
    
    
  }

  @Override
  protected AbstractRecursiveConverterTask<ArteMVideoDto, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArteMVideoTask(crawler, aElementsToProcess, getAuthKey().orElse(""), subPageIndex+1);
  }
  
    
}
