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
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnEpisodeDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnEpisodeTask extends AbstractJsonRestTask<OrfOnVideoInfoDTO, OrfOnVideoInfoDTO, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeTask.class);

  public OrfOnEpisodeTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.bearer);
  }

  @Override
  protected JsonDeserializer<OrfOnVideoInfoDTO> getParser(TopicUrlDTO aDTO) {
    return new OrfOnEpisodeDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<OrfOnVideoInfoDTO>() {}.getType();
  }

  @Override
  protected void postProcessing(OrfOnVideoInfoDTO aResponseObj, TopicUrlDTO aDTO) {
    if (!aResponseObj.getTitle().isPresent()) {
      LOG.debug("Missing title for {}", aDTO);
      return;
    }
    if (aResponseObj.getTopic().isPresent()) {
      LOG.debug("Missing topic for {}", aDTO);
      return;
    }
    if (aResponseObj.getVideoUrls().isPresent()) {
      LOG.debug("Missing videoUrls for {}", aDTO);
      return;
    }
    LOG.debug(" bread crums {}", aDTO.getTopic() + " # " + aResponseObj.getTitle().get());
    taskResults.add(aResponseObj);    
  }

  @Override
  protected AbstractRecursiveConverterTask<OrfOnVideoInfoDTO, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new OrfOnEpisodeTask(crawler, aElementsToProcess);
  } 

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
      crawler.printErrorMessage();
      LOG.fatal(
          "A HTTP error {} occurred when getting REST information from: \"{}\".",
          response.getStatus(),
          url);
  }










}
