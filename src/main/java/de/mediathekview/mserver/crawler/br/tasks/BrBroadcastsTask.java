package de.mediathekview.mserver.crawler.br.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractGraphQlTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.br.BrClipQueryDto;
import de.mediathekview.mserver.crawler.br.BrConstants;
import de.mediathekview.mserver.crawler.br.BrQueryDto;
import de.mediathekview.mserver.crawler.br.data.BrClipCollectIDResult;
import de.mediathekview.mserver.crawler.br.json.BrProgramIdsDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BrBroadcastsTask
    extends AbstractGraphQlTask<BrClipQueryDto, BrClipCollectIDResult, BrQueryDto> {

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
  protected void handleHttpError(BrQueryDto dto, URI url, Response response) {
    crawler.printErrorMessage();
    LOG.error(
        "HTTP error {}: start: {}, end: {}, page: {}.",
        response.getStatus(),
        dto.getStart(),
        dto.getEnd(),
        dto.getPageSize());
    crawler.incrementAndGetErrorCount();
  }

  @Override
  protected void postProcessing(BrClipCollectIDResult responseObj, BrQueryDto dto) {

    responseObj
        .getClipList()
        .getIds()
        .forEach(id -> taskResults.add(new BrClipQueryDto(BrConstants.GRAPHQL_API, id)));

    if (responseObj.hasNextPage()) {
      final ConcurrentLinkedQueue<BrQueryDto> crawlerUrls = new ConcurrentLinkedQueue<>();
      BrQueryDto nextPage =
          new BrQueryDto(
              dto.getUrl(),
              dto.getBroadcastServiceName(),
              dto.getStart(),
              dto.getEnd(),
              dto.getPageSize(),
              Optional.of(responseObj.getCursor()));
      crawlerUrls.add(nextPage);
      taskResults.addAll(createNewOwnInstance(crawlerUrls).invoke());
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<BrClipQueryDto, BrQueryDto> createNewOwnInstance(
      Queue<BrQueryDto> elementsToProcess) {
    return new BrBroadcastsTask(crawler, elementsToProcess);
  }
}
