package mServer.crawler.sender.arte.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import mServer.crawler.sender.arte.json.ArteVideoInfoDto;
import mServer.crawler.sender.arte.json.ArteVideoLinkDeserializer;
import mServer.crawler.sender.arte.json.ArteVideoLinkDto;
import jakarta.ws.rs.core.Response;

public class ArteVideoLinkTask
    extends AbstractJsonRestTask<ArteVideoInfoDto, List<ArteVideoLinkDto>, ArteVideoInfoDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());

  
  public ArteVideoLinkTask(MediathekReader crawler, ConcurrentLinkedQueue<ArteVideoInfoDto> videoInfo) {
    super(crawler, videoInfo, Optional.empty());
  }
  
  @Override
  protected JsonDeserializer<List<ArteVideoLinkDto>> getParser(ArteVideoInfoDto aDTO) {
    return new ArteVideoLinkDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<List<ArteVideoLinkDto>>() {}.getType();
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
  protected AbstractRecursivConverterTask<ArteVideoInfoDto, ArteVideoInfoDto> createNewOwnInstance(
          ConcurrentLinkedQueue<ArteVideoInfoDto> aElementsToProcess) {
    return new ArteVideoLinkTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(ArteVideoInfoDto dto, URI url, Response response) {
    Log.errorLog(89723823, "http error: " + response.getStatus() + " " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoLink information from: \"{}\".", response.getStatus(), url);
  }
}
