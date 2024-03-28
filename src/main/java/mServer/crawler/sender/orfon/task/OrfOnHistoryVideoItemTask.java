package mServer.crawler.sender.orfon.task;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.PagedElementListDTO;
import mServer.crawler.sender.orfon.OrfOnBreadCrumsUrlDTO;
import mServer.crawler.sender.orfon.json.OrfOnHistoryVideoItemDeserializer;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnHistoryVideoItemTask extends OrfOnPagedTask {
  private static final long serialVersionUID = 1L;

  public OrfOnHistoryVideoItemTask(MediathekReader crawler, ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
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
  protected AbstractRecursivConverterTask<OrfOnBreadCrumsUrlDTO, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnHistoryVideoItemTask(crawler, aElementsToProcess);
  }

}
