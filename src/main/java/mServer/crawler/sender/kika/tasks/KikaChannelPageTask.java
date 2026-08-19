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
import mServer.crawler.sender.kika.json.KikaChannelPageDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaChannelPageTask
    extends AbstractJsonRestTask<KikaEntityDto, PagedElementListDTO<KikaEntityDto>, KikaEntityDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursivConverterTask<KikaEntityDto, KikaEntityDto>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaChannelPageTask(MediathekReader crawler, ConcurrentLinkedQueue<KikaEntityDto> urlToCrawlDTOs, int maxSubpages) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
    this.maxSubpages = maxSubpages;
  }
  
  @Override
  protected JsonDeserializer<PagedElementListDTO<KikaEntityDto>> getParser(KikaEntityDto aDTO) {
    return new KikaChannelPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<KikaEntityDto>>() {}.getType();
  }

  protected void postProcessingNextPage(PagedElementListDTO<KikaEntityDto> aResponseObj, KikaEntityDto aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    if (aResponseObj.getNextPage().get().contains("page="+maxSubpages+"&")) {
      log.debug("stop at page url {} due to limit {}", aResponseObj.getNextPage().get(), maxSubpages);
      return;
    }
    
    final ConcurrentLinkedQueue<KikaEntityDto> nextPageLinks = new ConcurrentLinkedQueue<>();
    KikaEntityDto next = new KikaEntityDto(aDTO.getDocType(), aDTO.getId(), aDTO.getUuid(), aDTO.getExternalId(), aDTO.getUrlPath(), aDTO.getApiId(), aResponseObj.getNextPage(), aDTO.getModificationDate());
    nextPageLinks.add(next);
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
    //log.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
  }
  
  protected void postProcessingElements(Set<KikaEntityDto> elements, CrawlerUrlDTO aDTO) {
    for (KikaEntityDto element : elements)  {
      if(!taskResults.add(element)) {
        //log.debug("Duplicate KikaChannelPageTask {} from {}", element.getUrl() , aDTO.getUrl());
      }
    }
  }
  
  @Override
  protected void postProcessing(PagedElementListDTO<KikaEntityDto> aResponseObj, KikaEntityDto aDTO) {
    postProcessingNextPage(aResponseObj, aDTO);
    postProcessingElements(aResponseObj.getElements(), aDTO);
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join(), aDTO));

  }

  @Override
  protected AbstractRecursivConverterTask<KikaEntityDto, KikaEntityDto> createNewOwnInstance(
      ConcurrentLinkedQueue<KikaEntityDto> aElementsToProcess) {
    return new KikaChannelPageTask(crawler, aElementsToProcess, maxSubpages);
  }

  @Override
  protected void handleHttpError(KikaEntityDto dto, URI url, Response response) {
    Log.errorLog(374323225, "http error " + response.getStatus() + ": " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
