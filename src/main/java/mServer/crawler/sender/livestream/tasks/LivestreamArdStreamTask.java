package mServer.crawler.sender.livestream.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.livestream.json.LivestreamArdStreamDeserializer;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LivestreamArdStreamTask extends AbstractJsonRestTask<TopicUrlDTO, Set<CrawlerUrlDTO>, TopicUrlDTO> {
  private static final long serialVersionUID = -1203126035944523033L;
  private static final Logger LOG = LogManager.getLogger(LivestreamArdStreamTask.class);

  public LivestreamArdStreamTask(MediathekReader crawler, ConcurrentLinkedQueue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
  }

  @Override
  protected JsonDeserializer<Set<CrawlerUrlDTO>> getParser(TopicUrlDTO aDTO) {
    return new LivestreamArdStreamDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<CrawlerUrlDTO>(){}.getType();
  }

  @Override
  protected void postProcessing(Set<CrawlerUrlDTO> aResponseObj, TopicUrlDTO aDTO) {
    aResponseObj.forEach(url -> {
      taskResults.add(new TopicUrlDTO(aDTO.getTopic(), url.getUrl()));
    });

  }

  @Override
  protected AbstractRecursivConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new LivestreamArdStreamTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
    // TODO log
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
  }


}
