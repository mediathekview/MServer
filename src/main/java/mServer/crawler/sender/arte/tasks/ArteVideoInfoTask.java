package mServer.crawler.sender.arte.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.base.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import mServer.crawler.sender.arte.ArteConstants;
import mServer.crawler.sender.arte.json.ArteVideoInfoDeserializer;
import mServer.crawler.sender.arte.json.ArteVideoInfoDto;
import jakarta.ws.rs.core.Response;

public class ArteVideoInfoTask
    extends AbstractJsonRestTask<ArteVideoInfoDto, PagedElementListDTO<ArteVideoInfoDto>, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected transient Optional<AbstractRecursivConverterTask<ArteVideoInfoDto, TopicUrlDTO>> nextPageTask = Optional.empty();

  
  public ArteVideoInfoTask(MediathekReader crawler, ConcurrentLinkedQueue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.of(ArteConstants.API_TOKEN));
  }
  
  @Override
  protected JsonDeserializer<PagedElementListDTO<ArteVideoInfoDto>> getParser(TopicUrlDTO aDTO) {
    return new ArteVideoInfoDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<ArteVideoInfoDto>>() {}.getType();
  }

  protected void postProcessingNextPage(PagedElementListDTO<ArteVideoInfoDto> aResponseObj) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    int maxPages = Math.min(100, getMaximumSubpages());
    if (aResponseObj.getNextPage().get().contains("age="+maxPages)) {
      log.debug("stop at page url {} due to limit {}", aResponseObj.getNextPage().get(), maxPages);
      return;
    }
    
    final ConcurrentLinkedQueue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.add(new TopicUrlDTO(aResponseObj.getNextPage().get(), aResponseObj.getNextPage().get()));
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
  }

  private int getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 10;
    } else {
      return 3;
    }
  }

  protected void postProcessingElements(Set<ArteVideoInfoDto> elements) {
    for (ArteVideoInfoDto element : elements)  {
      taskResults.add(element);
    }
  }
  
  @Override
  protected void postProcessing(PagedElementListDTO<ArteVideoInfoDto> aResponseObj, TopicUrlDTO aDTO) {
    postProcessingNextPage(aResponseObj);
    postProcessingElements(aResponseObj.getElements());
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join()));

  }

  @Override
  protected AbstractRecursivConverterTask<ArteVideoInfoDto, TopicUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new ArteVideoInfoTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
    Log.errorLog(45983789, "http error " + response.getStatus() + ": " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
