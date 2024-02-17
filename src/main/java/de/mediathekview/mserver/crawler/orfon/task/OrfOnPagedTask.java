package de.mediathekview.mserver.crawler.orfon.task;

import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public abstract class OrfOnPagedTask extends AbstractJsonRestTask<OrfOnBreadCrumsUrlDTO, PagedElementListDTO<OrfOnBreadCrumsUrlDTO>, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 1L;
  protected final Logger LOG = LogManager.getLogger(this.getClass());
  protected Optional<AbstractRecursiveConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO>> nextPageTask = Optional.empty();

  public OrfOnPagedTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.bearer);
  }
  
  protected void postProcessingNextPage(PagedElementListDTO<OrfOnBreadCrumsUrlDTO> aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    final Queue<OrfOnBreadCrumsUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    nextPageLinks.add(new OrfOnBreadCrumsUrlDTO(aDTO.getBreadCrums(), aResponseObj.getNextPage().get()));
    nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
    nextPageTask.get().fork();
    LOG.debug("started paging to url {} for {}", aResponseObj.getNextPage().get(), aDTO.getUrl());
  }
  
  protected void postProcessingElements(Set<OrfOnBreadCrumsUrlDTO> elements, OrfOnBreadCrumsUrlDTO originalDTO) {
    for (OrfOnBreadCrumsUrlDTO element : elements)  {
      element.setBreadCrumsPath(originalDTO.getBreadCrums());
      taskResults.add(element);
    }
  }

  @Override
  protected void postProcessing(PagedElementListDTO<OrfOnBreadCrumsUrlDTO> aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    postProcessingNextPage(aResponseObj, aDTO);
    postProcessingElements(aResponseObj.getElements(), aDTO);
    nextPageTask.ifPresent(paginationResults -> postProcessingElements(paginationResults.join(), aDTO));
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
