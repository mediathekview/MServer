package mServer.crawler.sender.livestream.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.livestream.json.LivestreamArdDeserializer;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LivestreamArdOverviewTask extends AbstractJsonRestTask<TopicUrlDTO, Set<TopicUrlDTO>, CrawlerUrlDTO> {
  private static final long serialVersionUID = -1203126035944523033L;
  private static final Logger LOG = LogManager.getLogger(LivestreamArdOverviewTask.class);

  public LivestreamArdOverviewTask(MediathekReader crawler, ConcurrentLinkedQueue<CrawlerUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
  }

  @Override
  protected JsonDeserializer<Set<TopicUrlDTO>> getParser(CrawlerUrlDTO aDTO) {
    return new LivestreamArdDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<TopicUrlDTO>() {
    }.getType();
  }

  @Override
  protected AbstractRecursivConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new LivestreamArdOverviewTask(crawler, aElementsToProcess);
  }

  @Override
  protected void postProcessing(Set<TopicUrlDTO> aResponseObj, CrawlerUrlDTO aDTO) {
    taskResults.addAll(aResponseObj);

  }

  @Override
  protected void handleHttpError(CrawlerUrlDTO dto, URI url, Response response) {
    // TODO log
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);

  }
}
