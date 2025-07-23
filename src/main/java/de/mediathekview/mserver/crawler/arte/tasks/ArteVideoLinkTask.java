package de.mediathekview.mserver.crawler.arte.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoInfoDto;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoLinkDeserializer;
import de.mediathekview.mserver.crawler.arte.json.ArteVideoLinkDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import jakarta.ws.rs.core.Response;

//return T Class from this task, desirialisation of class R , D , Reasearch in this url

public class ArteVideoLinkTask
    extends AbstractJsonRestTask<ArteVideoInfoDto, List<ArteVideoLinkDto>, ArteVideoInfoDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());

  
  public ArteVideoLinkTask(AbstractCrawler crawler, Queue<ArteVideoInfoDto> videoInfo) {
    super(crawler, videoInfo, ArteConstants.API_TOKEN);
  }
  
  @Override
  protected JsonDeserializer<List<ArteVideoLinkDto>> getParser(ArteVideoInfoDto aDTO) {
    return new ArteVideoLinkDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<ArteVideoLinkDto>>() {}.getType();
  }

  protected void postProcessingElements(List<ArteVideoLinkDto> elements, ArteVideoInfoDto aDTO) {
    aDTO.setVideoLinks(elements);
    taskResults.add(aDTO);
  }
  
  @Override
  protected void postProcessing(List<ArteVideoLinkDto> aResponseObj, ArteVideoInfoDto aDTO) {
    postProcessingElements(aResponseObj, aDTO);
  }

  @Override
  protected AbstractRecursiveConverterTask<ArteVideoInfoDto, ArteVideoInfoDto> createNewOwnInstance(
      Queue<ArteVideoInfoDto> aElementsToProcess) {
    return new ArteVideoLinkTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(ArteVideoInfoDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    log.fatal("A HTTP error {} occurred when getting REST VideoLink information from: \"{}\".", response.getStatus(), url);
    crawler.incrementAndGetErrorCount();
  }
}
