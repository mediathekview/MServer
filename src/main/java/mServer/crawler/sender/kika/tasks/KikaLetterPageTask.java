package mServer.crawler.sender.kika.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.kika.KikaEntityDto;
import mServer.crawler.sender.kika.json.KikaLetterPageDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaLetterPageTask
    extends AbstractJsonRestTask<KikaEntityDto, PagedElementListDTO<KikaEntityDto>, CrawlerUrlDTO> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursivConverterTask<KikaEntityDto, CrawlerUrlDTO>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaLetterPageTask(MediathekReader crawler, ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs, int maxSubpages) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
    this.maxSubpages = maxSubpages;
  }
  
  @Override
  protected JsonDeserializer<PagedElementListDTO<KikaEntityDto>> getParser(CrawlerUrlDTO aDTO) {
    return new KikaLetterPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<KikaEntityDto>>() {}.getType();
  }

  protected void postProcessingNextPage(PagedElementListDTO<KikaEntityDto> aResponseObj, CrawlerUrlDTO aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    if (aResponseObj.getNextPage().get().contains("page="+maxSubpages+"&")) {
      log.debug("stop at page url {} due to limit {}", aResponseObj.getNextPage().get(), maxSubpages);
      return;
    }
    
    final ConcurrentLinkedQueue<CrawlerUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.add(new CrawlerUrlDTO(aResponseObj.getNextPage().get()));
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
    //log.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
  }
  
  protected void postProcessingElements(Set<KikaEntityDto> elements, CrawlerUrlDTO aDTO) {
    for (KikaEntityDto element : elements)  {
      if(!taskResults.add(element)) {
        //log.debug("Duplicate KikaLetterPageTask {} from {}", element.getUrl() , aDTO.getUrl());
      }
    }
  }
  
  @Override
  protected void postProcessing(PagedElementListDTO<KikaEntityDto> aResponseObj, CrawlerUrlDTO aDTO) {
    postProcessingNextPage(aResponseObj, aDTO);
    postProcessingElements(aResponseObj.getElements(), aDTO);
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join(), aDTO));

  }

  @Override
  protected AbstractRecursivConverterTask<KikaEntityDto, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaLetterPageTask(crawler, aElementsToProcess, maxSubpages);
  }

  @Override
  protected void handleHttpError(CrawlerUrlDTO dto, URI url, Response response) {
    Log.errorLog(374323226, "http error " + response.getStatus() + ": " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
