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
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnEpisodesDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnEpisodesTask extends AbstractJsonRestTask<OrfOnVideoInfoDTO, PagedElementListDTO<OrfOnVideoInfoDTO>, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodesTask.class);

  public OrfOnEpisodesTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.AUTH);
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<OrfOnVideoInfoDTO>> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnEpisodesDeserializer(this.crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<OrfOnVideoInfoDTO>>() {}.getType();
  }

  @Override
  protected void postProcessing(PagedElementListDTO<OrfOnVideoInfoDTO> aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    final Optional<AbstractRecursiveConverterTask<OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO>> subpageCrawler;
    if (aResponseObj.getNextPage().isPresent()) {
      final Queue<OrfOnBreadCrumsUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
      nextPageLinks.add(new OrfOnBreadCrumsUrlDTO("", aResponseObj.getNextPage().get()));
      subpageCrawler = Optional.of(createNewOwnInstance(nextPageLinks));
      subpageCrawler.get().fork();
      LOG.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
    } else {
      subpageCrawler = Optional.empty();
    }
    //
    for (OrfOnVideoInfoDTO rs : aResponseObj.getElements()) {
      if (rs.getTitle().isEmpty() && rs.getTitleWithDate().isEmpty()) {
        LOG.warn("Missing title for {} in {}", rs.getId(), aDTO);
        crawler.incrementAndGetErrorCount();
        return;
      }
      if (rs.getTopic().isEmpty()) {
        LOG.warn("Missing topic for {} in {}", rs.getId(), aDTO);
        crawler.incrementAndGetErrorCount();
        return;
      }
      if (rs.getVideoUrls().isEmpty()) {
        LOG.warn("Missing videoUrls for {} in {}", rs.getId(), aDTO);
        crawler.incrementAndGetErrorCount();
        return;
      }
      if (rs.getDuration().isEmpty()) {
        LOG.warn("Missing duration for {} in {}", rs.getId(), aDTO);
      }
      if (rs.getAired().isEmpty()) {
        LOG.warn("Missing aired date for {} in {}", rs.getId(), aDTO);
      }
      if (rs.getWebsite().isEmpty()) {
        LOG.warn("Missing website for {} in {}", rs.getId(), aDTO);
      }
      taskResults.add(rs);
    }
    subpageCrawler.ifPresent(paginationResults -> taskResults.addAll(paginationResults.join()));
  }

  @Override
  protected AbstractRecursiveConverterTask<OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(Queue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnEpisodesTask(crawler, aElementsToProcess);
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
