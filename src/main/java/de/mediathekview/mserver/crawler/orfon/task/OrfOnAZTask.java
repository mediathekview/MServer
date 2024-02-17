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
import de.mediathekview.mserver.crawler.orfon.json.OrfOnAZDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnAZTask extends AbstractJsonRestTask<TopicUrlDTO, PagedElementListDTO<TopicUrlDTO>, TopicUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnAZTask.class);

  public OrfOnAZTask(AbstractCrawler crawler, Queue<TopicUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.bearer);
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<TopicUrlDTO>> getParser(TopicUrlDTO aDTO) {
    return new OrfOnAZDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<TopicUrlDTO>>() {}.getType();
  }

  @Override
  protected void postProcessing(PagedElementListDTO<TopicUrlDTO> aResponseObj, TopicUrlDTO aDTO) {
    final Optional<AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO>> subpageCrawler;
    if (aResponseObj.getNextPage().isPresent()) {
      final Queue<TopicUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new TopicUrlDTO("", aResponseObj.getNextPage().get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
      LOG.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
    } else {
      subpageCrawler = Optional.empty();
    }

    taskResults.addAll(aResponseObj.getElements());
    subpageCrawler.ifPresent(paginationResults -> taskResults.addAll(paginationResults.join()));
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, TopicUrlDTO> createNewOwnInstance(Queue<TopicUrlDTO> aElementsToProcess) {
    return new OrfOnAZTask(crawler, aElementsToProcess);
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
