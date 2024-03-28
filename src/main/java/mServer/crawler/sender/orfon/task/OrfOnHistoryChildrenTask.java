package mServer.crawler.sender.orfon.task;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.orfon.OrfOnBreadCrumsUrlDTO;
import mServer.crawler.sender.orfon.json.OrfOnHistoryChildrenDeserializer;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnHistoryChildrenTask extends OrfOnPagedTask {
  private static final long serialVersionUID = 1L;

  public OrfOnHistoryChildrenTask(MediathekReader crawler, ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
  }

  @Override
  protected JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnHistoryChildrenDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>>() {}.getType();
  }

  @Override
  protected void postProcessingElements(Set<OrfOnBreadCrumsUrlDTO> elements, OrfOnBreadCrumsUrlDTO originalDTO) {
    for (OrfOnBreadCrumsUrlDTO element : elements)  {
      if (element.getUrl().contains("/children")) {
        final ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> moreContentOnNewPage = new ConcurrentLinkedQueue<>();
        moreContentOnNewPage.add(element);
        AbstractRecursivConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> resolveChildren = createNewOwnInstance(moreContentOnNewPage);
        resolveChildren.fork();
        for(OrfOnBreadCrumsUrlDTO moreElements : resolveChildren.join()) {
          moreElements.setBreadCrumsPath(originalDTO.getBreadCrums());
          taskResults.add(moreElements);
        }
      } else {
        element.setBreadCrumsPath(originalDTO.getBreadCrums());
        taskResults.add(element);
      }
    }
  }
  
  @Override
  protected AbstractRecursivConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnHistoryChildrenTask(crawler, aElementsToProcess);
  }

}
