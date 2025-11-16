package de.mediathekview.mserver.crawler.arte.tasks;

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

import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import jakarta.ws.rs.core.Response;

//return T Class from this task, desirialisation of class R , D , Reasearch in this url

public class ArteVideoInfoTask
    extends AbstractJsonRestTask<ArteVideoInfoDto, PagedElementListDTO<ArteVideoInfoDto>, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursiveConverterTask<ArteVideoInfoDto, TopicUrlDTO>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public ArteVideoInfoTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs, int maxSubpages) {
    super(crawler, urlToCrawlDTOs, ArteConstants.API_TOKEN);
    this.maxSubpages = maxSubpages;
  }
  
  @Override
  protected JsonDeserializer<PagedElementListDTO<ArteVideoInfoDto>> getParser(TopicUrlDTO aDTO) {
    return new ArteVideoInfoDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<ArteVideoInfoDto>>() {}.getType();
  }

  protected void postProcessingNextPage(PagedElementListDTO<ArteVideoInfoDto> aResponseObj, TopicUrlDTO aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    if (aResponseObj.getNextPage().get().contains("page="+maxSubpages+"&")) {
      log.debug("stop at page url {} due to limit {}", aResponseObj.getNextPage().get(), maxSubpages);
      return;
    }
    
    final Queue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.add(new TopicUrlDTO(aResponseObj.getNextPage().get(), aResponseObj.getNextPage().get()));
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
    //log.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
  }
  
  protected void postProcessingElements(Set<ArteVideoInfoDto> elements, TopicUrlDTO aDTO) {
    for (ArteVideoInfoDto element : elements)  {
      taskResults.add(element);
    }
  }
  
  @Override
  protected void postProcessing(PagedElementListDTO<ArteVideoInfoDto> aResponseObj, TopicUrlDTO aDTO) {
    postProcessingNextPage(aResponseObj, aDTO);
    postProcessingElements(aResponseObj.getElements(), aDTO);
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join(), aDTO));

  }

  @Override
  protected AbstractRecursiveConverterTask<ArteVideoInfoDto, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new ArteVideoInfoTask(crawler, aElementsToProcess, maxSubpages);
  }

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
    crawler.printErrorMessage();
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
