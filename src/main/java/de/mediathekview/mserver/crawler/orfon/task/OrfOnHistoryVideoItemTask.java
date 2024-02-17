package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.util.Queue;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnHistoryVideoItemDeserializer;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnHistoryVideoItemTask extends OrfOnPagedTask {
  private static final long serialVersionUID = 1L;

  public OrfOnHistoryVideoItemTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs);
  }  

  @Override
  protected JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnHistoryVideoItemDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>>() {}.getType();
  }

  @Override
  protected AbstractRecursiveConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(Queue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnHistoryVideoItemTask(crawler, aElementsToProcess);
  }

}
