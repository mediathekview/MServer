package mServer.crawler.sender.kika.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.kika.KikaAssetDto;
import mServer.crawler.sender.kika.KikaEntityDto;
import mServer.crawler.sender.kika.KikaFilmDto;
import mServer.crawler.sender.kika.json.KikaAssetPageDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaAssetsPageTask
    extends AbstractJsonRestTask<KikaFilmDto, List<KikaAssetDto>, KikaFilmDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursivConverterTask<KikaEntityDto, CrawlerUrlDTO>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaAssetsPageTask(MediathekReader crawler, ConcurrentLinkedQueue<KikaFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
  }
  
  @Override
  protected JsonDeserializer<List<KikaAssetDto>> getParser(KikaFilmDto aDTO) {
    return new KikaAssetPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<List<KikaAssetDto>>() {}.getType();
  }
  
  @Override
  protected void postProcessing(List<KikaAssetDto> aResponseObj, KikaFilmDto aDTO) {
    if(!aResponseObj.isEmpty()) {
      aDTO.setAssets(aResponseObj);
      taskResults.add(aDTO);
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaFilmDto, KikaFilmDto> createNewOwnInstance(
      ConcurrentLinkedQueue<KikaFilmDto> aElementsToProcess) {
    return new KikaAssetsPageTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(KikaFilmDto dto, URI url, Response response) {
    Log.errorLog(374323223, "http error " + response.getStatus() + ": " + url);
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
