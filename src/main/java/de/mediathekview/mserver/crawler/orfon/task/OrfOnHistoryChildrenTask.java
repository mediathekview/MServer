package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnHistoryChildrenDeserializer;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnHistoryDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnHistoryChildrenTask extends AbstractJsonRestTask<TopicUrlDTO, PagedElementListDTO<TopicUrlDTO>, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnHistoryChildrenTask.class);

  public OrfOnHistoryChildrenTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.bearer);
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<TopicUrlDTO>> getParser(TopicUrlDTO aDTO) {
    return new OrfOnHistoryChildrenDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<TopicUrlDTO>>() {}.getType();
  }

  @Override
  protected void postProcessing(PagedElementListDTO<TopicUrlDTO> aResponseObj, TopicUrlDTO aDTO) {
    for (TopicUrlDTO childOrVideo : aResponseObj.getElements()) {
      if (childOrVideo.getUrl().endsWith("children")) {
        final Queue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
        nextPageLinks.add(childOrVideo);
        AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> resolveChildren = createNewOwnInstance(nextPageLinks);
        resolveChildren.fork();
        taskResults.addAll(resolveChildren.join());
      } else {
        taskResults.add(new TopicUrlDTO(aDTO.getTopic() + " # " + childOrVideo.getTopic(), childOrVideo.getUrl() + "?limit=99"));
        //taskResults.add(childOrVideo);
      }
    }
    //taskResults.addAll(aResponseObj.getElements());
    //
    final Optional<AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO>> subpageCrawler;
    if (aResponseObj.getNextPage().isPresent()) {
      final Queue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new TopicUrlDTO(aDTO.getTopic(), aResponseObj.getNextPage().get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
      LOG.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
    } else {
      subpageCrawler = Optional.empty();
    }
    subpageCrawler.ifPresent(paginationResults -> taskResults.addAll(paginationResults.join()));
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(Queue<TopicUrlDTO> aElementsToProcess) {
    return new OrfOnHistoryChildrenTask(crawler, aElementsToProcess);
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
