package de.mediathekview.mserver.crawler.kika.tasks;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaAssetDto;
import de.mediathekview.mserver.crawler.kika.KikaEntityDto;
import de.mediathekview.mserver.crawler.kika.KikaFilmDto;
import de.mediathekview.mserver.crawler.kika.json.KikaAssetPageDeserializer;
import jakarta.ws.rs.core.Response;

public class KikaAssetsPageTask
    extends AbstractJsonRestTask<KikaFilmDto, List<KikaAssetDto>, KikaFilmDto> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursiveConverterTask<KikaEntityDto, CrawlerUrlDTO>> nextPageTask = Optional.empty();
  protected int maxSubpages;

  
  public KikaAssetsPageTask(AbstractCrawler crawler, Queue<KikaFilmDto> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, null);
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
    if(aResponseObj.size() > 0) {
      aDTO.setAssets(aResponseObj);
      taskResults.add(aDTO);
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<KikaFilmDto, KikaFilmDto> createNewOwnInstance(
      Queue<KikaFilmDto> aElementsToProcess) {
    return new KikaAssetsPageTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(KikaFilmDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    log.fatal("A HTTP error {} occurred when getting REST VideoInfo information from: \"{}\".", response.getStatus(), url);
  }
}
