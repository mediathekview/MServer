package de.mediathekview.mserver.crawler.br.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractGraphQlTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.br.BrQueryDto;
import de.mediathekview.mserver.crawler.br.data.BrClipCollectIDResult;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.json.BrProgramIdsDeserializer;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrBroadcastsTask extends AbstractGraphQlTask<BrID, BrClipCollectIDResult, BrQueryDto> {

  private static final Logger LOG = LogManager.getLogger(BrBroadcastsTask.class);

  public BrBroadcastsTask(AbstractCrawler crawler, Queue<BrQueryDto> urlToCrawlDtos) {
    super(crawler, urlToCrawlDtos, null);
  }

  @Override
  protected Object getParser(BrQueryDto dto) {
    return new BrProgramIdsDeserializer();
  }

  @Override
  protected Type getType() {
    return BrClipCollectIDResult.class;
  }

  @Override
  protected void handleHttpError(URI url, Response response) {
    crawler.printErrorMessage();
    LOG.error(
        "A HTTP error {} occurred when getting REST information from: \"{}\".",
        response.getStatus(),
        url);
    crawler.incrementAndGetErrorCount();
  }

  @Override
  protected void postProcessing(BrClipCollectIDResult responseObj, BrQueryDto dto) {

    taskResults.addAll(responseObj.getClipList().getIds());

    if (responseObj.hasNextPage()) {
      final ConcurrentLinkedQueue<BrQueryDto> crawlerUrls = new ConcurrentLinkedQueue<>();
      BrQueryDto nextPage =
          new BrQueryDto(
              dto.getUrl(),
              dto.getStart(),
              dto.getEnd(),
              dto.getPageSize(),
              Optional.of(responseObj.getCursor()));
      crawlerUrls.add(nextPage);
      taskResults.addAll(createNewOwnInstance(crawlerUrls).invoke());
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<BrID, BrQueryDto> createNewOwnInstance(
      Queue<BrQueryDto> elementsToProcess) {
    return new BrBroadcastsTask(crawler, elementsToProcess);
  }
}
