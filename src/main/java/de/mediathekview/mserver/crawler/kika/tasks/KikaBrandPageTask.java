package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.KikaEntityDto;
import de.mediathekview.mserver.crawler.kika.json.KikaBrandPageDeserializer;
import jakarta.ws.rs.core.Response;

public class KikaBrandPageTask
    extends AbstractJsonRestTask<KikaEntityDto, Optional<KikaEntityDto>, KikaEntityDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursiveConverterTask<KikaEntityDto, KikaEntityDto>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaBrandPageTask(AbstractCrawler crawler, Queue<KikaEntityDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
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
  protected AbstractRecursiveConverterTask<KikaEntityDto, KikaEntityDto> createNewOwnInstance(
      Queue<KikaEntityDto> aElementsToProcess) {
    return new KikaBrandPageTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(KikaEntityDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
