package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Queue;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnScheduleDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnScheduleTask extends AbstractJsonRestTask<TopicUrlDTO, Set<TopicUrlDTO>, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnScheduleTask.class);

  public OrfOnScheduleTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.bearer);
  }

  @Override
  protected JsonDeserializer<Set<TopicUrlDTO>> getParser(TopicUrlDTO aDTO) {
    return new OrfOnScheduleDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<TopicUrlDTO>>() {}.getType();
  }

  @Override
  protected void postProcessing(Set<TopicUrlDTO> aResponseObj, TopicUrlDTO aDTO) {
    taskResults.addAll(aResponseObj);
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
      Queue<TopicUrlDTO> aElementsToProcess) {
    return new OrfOnScheduleTask(crawler, aElementsToProcess);
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
