package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.kika.KikaEntityDto;
import de.mediathekview.mserver.crawler.kika.KikaFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaVideoSubchannelPageDeserializer;
import jakarta.ws.rs.core.Response;

public class KikaVideoSubchannelPageTask
    extends AbstractJsonRestTask<KikaFilmDto, PagedElementListDTO<KikaFilmDto>, KikaEntityDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursiveConverterTask<KikaFilmDto, KikaEntityDto>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaVideoSubchannelPageTask(AbstractCrawler crawler, Queue<KikaEntityDto> urlToCrawlDTOs, int maxSubpages) {
    super(crawler, urlToCrawlDTOs, null);
    this.maxSubpages = maxSubpages;
  }
  
  @Override
  protected JsonDeserializer<PagedElementListDTO<KikaFilmDto>> getParser(KikaEntityDto aDTO) {
    return new KikaVideoSubchannelPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<KikaFilmDto>>() {}.getType();
  }

  protected void postProcessingNextPage(PagedElementListDTO<KikaFilmDto> aResponseObj, KikaEntityDto aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    if (aResponseObj.getNextPage().get().contains("page="+maxSubpages+"&")) {
      log.debug("stop at page url {} due to limit {}", aResponseObj.getNextPage().get(), maxSubpages);
      return;
    }
    
    final Queue<KikaEntityDto> nextPageLinks = new ConcurrentLinkedQueue<>();
    KikaEntityDto next = new KikaEntityDto(aDTO.getDocType(), aDTO.getId(), aDTO.getUuid(), aDTO.getExternalId(), aDTO.getUrlPath(), aDTO.getApiId(), aResponseObj.getNextPage(), aDTO.getModificationDate());
    nextPageLinks.add(next);
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
    //log.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
  }
  
  protected void postProcessingElements(Set<KikaFilmDto> elements, KikaEntityDto aDTO) {
    for (KikaFilmDto element : elements)  {
      if(!taskResults.add(element)) {
        //log.debug("Duplicate KikaVideoSubchannelPageTask {} from {}", element.getUrl() , aDTO.getUrl());
      }
    }
  }
  
  @Override
  protected void postProcessing(PagedElementListDTO<KikaFilmDto> aResponseObj, KikaEntityDto aDTO) {
    postProcessingNextPage(aResponseObj, aDTO);
    postProcessingElements(aResponseObj.getElements(), aDTO);
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join(), aDTO));

  }

  @Override
  protected AbstractRecursiveConverterTask<KikaFilmDto, KikaEntityDto> createNewOwnInstance(
      Queue<KikaEntityDto> aElementsToProcess) {
    return new KikaVideoSubchannelPageTask(crawler, aElementsToProcess, maxSubpages);
  }

  @Override
  protected void handleHttpError(KikaEntityDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
