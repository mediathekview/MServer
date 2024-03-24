package mServer.crawler.sender.orfon.task;

import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.orfon.OrfOnBreadCrumsUrlDTO;
import mServer.crawler.sender.orfon.OrfOnConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public abstract class OrfOnPagedTask extends AbstractJsonRestTask<OrfOnBreadCrumsUrlDTO, PagedElementListDTO<OrfOnBreadCrumsUrlDTO>, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 1L;
  protected final transient Logger log = LogManager.getLogger(this.getClass());
  protected transient Optional<AbstractRecursivConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO>> nextPageTask = Optional.empty();

  protected OrfOnPagedTask(MediathekReader crawler, ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.of(OrfOnConstants.AUTH));
  }
  
  protected void postProcessingNextPage(PagedElementListDTO<OrfOnBreadCrumsUrlDTO> aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    if (aResponseObj.getNextPage().isEmpty()) {
      return;
    }
    final ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> nextPageLinks = new ConcurrentLinkedQueue<>();
    final Optional<String> nextPage = aResponseObj.getNextPage();
    if (nextPage.isPresent()) {
      nextPageLinks.add(new OrfOnBreadCrumsUrlDTO(aDTO.getBreadCrums(), nextPage.get()));
      nextPageTask = Optional.of(createNewOwnInstance(nextPageLinks));
      nextPageTask.get().fork();
      log.debug("started paging to url {} for {}", nextPage.get(), aDTO.getUrl());
    }
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
    Log.errorLog(874764622, "ORF: http error " + response.getStatus() + ", " + url);
      log.fatal(
          "A HTTP error {} occurred when getting REST information from: \"{}\".",
          response.getStatus(),
          url);
  }

}
