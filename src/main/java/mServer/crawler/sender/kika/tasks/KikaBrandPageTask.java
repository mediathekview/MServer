package mServer.crawler.sender.kika.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.kika.KikaEntityDto;
import mServer.crawler.sender.kika.json.KikaBrandPageDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaBrandPageTask
    extends AbstractJsonRestTask<KikaEntityDto, Optional<KikaEntityDto>, KikaEntityDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursivConverterTask<KikaEntityDto, KikaEntityDto>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaBrandPageTask(MediathekReader crawler, ConcurrentLinkedQueue<KikaEntityDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
  }
  
  @Override
  protected JsonDeserializer<Optional<KikaEntityDto>> getParser(KikaEntityDto aDTO) {
    return new KikaBrandPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Optional<KikaEntityDto>>() {}.getType();
  }
  
  @Override
  protected void postProcessing(Optional<KikaEntityDto> aResponseObj, KikaEntityDto aDTO) {
    if(aResponseObj.isPresent()) {
      if(!taskResults.add(aResponseObj.get())) {
        //log.debug("Duplicate KikaBrandPageTask {} from {}", aResponseObj.get().getUrl() , aDTO.getUrl());
      }
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaEntityDto, KikaEntityDto> createNewOwnInstance(
      ConcurrentLinkedQueue<KikaEntityDto> aElementsToProcess) {
    return new KikaBrandPageTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(KikaEntityDto dto, URI url, Response response) {
    Log.errorLog(374323224, "http error " + response.getStatus() + ": " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
