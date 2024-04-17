package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnHistoryChildrenDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnHistoryChildrenTask extends OrfOnPagedTask {
  private static final long serialVersionUID = 1L;

  public OrfOnHistoryChildrenTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
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
        final Queue<OrfOnBreadCrumsUrlDTO> moreContentOnNewPage = new ConcurrentLinkedQueue<>();
        moreContentOnNewPage.add(element);
        AbstractRecursiveConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> resolveChildren = createNewOwnInstance(moreContentOnNewPage);
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
  protected AbstractRecursiveConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(Queue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnHistoryChildrenTask(crawler, aElementsToProcess);
  }

}
