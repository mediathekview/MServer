package mServer.crawler.sender.kika.tasks;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.kika.KikaApiFilmDto;
import mServer.crawler.sender.kika.KikaApiTopicDto;
import mServer.crawler.sender.kika.json.KikaApiTopicPageDeserializer;
import mServer.crawler.sender.orf.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class KikaApiTopicTask extends AbstractJsonRestTask<KikaApiFilmDto, KikaApiTopicDto, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(KikaApiTopicTask.class);
  private int subPageIndex = 0;
  private int maxPages = 1;

  public KikaApiTopicTask(MediathekReader crawler, ConcurrentLinkedQueue<TopicUrlDTO> urlToCrawlDTOs, int subPageIndex, int maxPages) {
    super(crawler, urlToCrawlDTOs, Optional.empty());
    this.subPageIndex = subPageIndex;
    this.maxPages = maxPages;
  }

  @Override
  protected JsonDeserializer<KikaApiTopicDto> getParser(TopicUrlDTO aDTO) {
    return new KikaApiTopicPageDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<Set<KikaApiTopicDto>>() {
    }.getType();
  }

  @Override
  protected void postProcessing(KikaApiTopicDto aResponseObj, TopicUrlDTO aDTO) {
    //
    aResponseObj.getErrorCode().ifPresent(errorCode -> {
      LOG.error("Error {} : {} for target {} ", errorCode, aResponseObj.getErrorMesssage().orElse(""), aDTO.getUrl());
      Log.errorLog(324978332, "Error %s : %s for target %s ".formatted(errorCode, aResponseObj.getErrorMesssage().orElse(""), aDTO.getUrl()));
      return;
    });
    //
    final Optional<AbstractRecursivConverterTask<KikaApiFilmDto, TopicUrlDTO>> subpageCrawler;
    final Optional<TopicUrlDTO> nextPageLink = aResponseObj.getNextPage();
    //
    if (nextPageLink.isPresent() && maxPages > subPageIndex) {
      final ConcurrentLinkedQueue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new TopicUrlDTO(aDTO.getTopic(), nextPageLink.get().getUrl())); // repack next page link to keep topic name
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }
    for (KikaApiFilmDto aFilm : aResponseObj.getElements()) {
      if (aFilm.getTopic().isEmpty()) {
        aFilm.setTopic(Optional.of(aDTO.getTopic()));
      }
      taskResults.add(aFilm);
    }
    //
    subpageCrawler.ifPresent(nextPageCrawler -> taskResults.addAll(nextPageCrawler.join()));
  }

  @Override
  protected void handleHttpError(TopicUrlDTO dto, URI url, Response response) {
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
    Log.errorLog(324978333, "A HTTP error %d occurred when getting REST information from: \"%s}\".".formatted(
            response.getStatus(),
            url));
  }

  @Override
  protected AbstractRecursivConverterTask<KikaApiFilmDto, TopicUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<TopicUrlDTO> aElementsToProcess) {
    return new KikaApiTopicTask(crawler, aElementsToProcess, subPageIndex + 1, maxPages);
  }


}
