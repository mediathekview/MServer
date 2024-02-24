package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Queue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnEpisodeDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnEpisodeTask extends AbstractJsonRestTask<OrfOnVideoInfoDTO, OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeTask.class);

  public OrfOnEpisodeTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.AUTH);
  }

  @Override
  protected JsonDeserializer<OrfOnVideoInfoDTO> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnEpisodeDeserializer(this.crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<OrfOnVideoInfoDTO>() {}.getType();
  }

  @Override
  protected void postProcessing(OrfOnVideoInfoDTO aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    if (aResponseObj.getTitle().isEmpty() && aResponseObj.getTitleWithDate().isEmpty()) {
      LOG.warn("Missing title for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getTopic().isEmpty()) {
      LOG.warn("Missing topic for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getVideoUrls().isEmpty()) {
      LOG.warn("Missing videoUrls for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    //LOG.debug(" bread crums {} # {} # {}", String.join("|", aDTO.getBreadCrums()), aResponseObj.getTopic().get(), aResponseObj.getTitle().get());
    taskResults.add(aResponseObj);    
  }

  @Override
  protected AbstractRecursiveConverterTask<OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(
      Queue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnEpisodeTask(crawler, aElementsToProcess);
  } 

  @Override
  protected void handleHttpError(OrfOnBreadCrumsUrlDTO dto, URI url, Response response) {
      crawler.printErrorMessage();
      LOG.fatal(
          "A HTTP error {} occurred when getting REST information from: \"{}\".",
          response.getStatus(),
          url);
  }

}
