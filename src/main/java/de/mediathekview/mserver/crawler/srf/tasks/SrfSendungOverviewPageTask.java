package de.mediathekview.mserver.crawler.srf.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.*;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungOverviewJsonDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SrfSendungOverviewPageTask extends AbstractRestTask<CrawlerUrlDTO, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(SrfSendungOverviewPageTask.class);

  private static final Type OPTIONAL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PagedElementListDTO<CrawlerUrlDTO>>>() {}.getType();

  private final int pageNumber;

  public SrfSendungOverviewPageTask(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    this(aCrawler, aUrlToCrawlDTOs, 1);
  }

  public SrfSendungOverviewPageTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      final int aPageNumber) {
    super(aCrawler, aUrlToCrawlDTOs, Optional.empty());

    pageNumber = aPageNumber;
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final String baseUrl = UrlUtils.getBaseUrl(aDTO.getUrl());

    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                OPTIONAL_DTO_TYPE_TOKEN, new SrfSendungOverviewJsonDeserializer(baseUrl))
            .create();
    final Invocation.Builder request = aTarget.request();
    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();

    if (response.getStatus() == 200) {

      final String jsonOutput = response.readEntity(String.class);

      try {
        final Optional<PagedElementListDTO<CrawlerUrlDTO>> resultDto =
            gson.fromJson(jsonOutput, OPTIONAL_DTO_TYPE_TOKEN);
        if (resultDto.isPresent()) {
          final PagedElementListDTO<CrawlerUrlDTO> dto = resultDto.get();
          taskResults.addAll(dto.getElements());

          final Optional<String> nextPageId = dto.getNextPage();
          if (nextPageId.isPresent()
              && pageNumber < crawler.getCrawlerConfig().getMaximumSubpages()) {
            processNextPage(nextPageId.get());
          }
        } else {
          LOG.error("SrfSendungOverviewPageTask: no result dto " + aTarget.getUri().toString());
        }
      } catch (final JsonSyntaxException e) {
        LOG.error(
            "SrfSendungOverviewPageTask: Error reading url " + aTarget.getUri().toString(), e);
      }
    } else {
      LOG.error(
          "SrfSendungOverviewPageTask: Error reading url "
              + aTarget.getUri().toString()
              + ": "
              + response.getStatus());
    }
  }

  @Override
  protected AbstractUrlTask createNewOwnInstance(final ConcurrentLinkedQueue aURLsToCrawl) {
    return new SrfSendungOverviewPageTask(crawler, aURLsToCrawl);
  }

  private void processNextPage(final String aNextPageId) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlDtos = new ConcurrentLinkedQueue<>();
    urlDtos.add(new CrawlerUrlDTO(aNextPageId));
    final Set<CrawlerUrlDTO> x =
        new SrfSendungOverviewPageTask(crawler, urlDtos, pageNumber + 1).invoke();
    taskResults.addAll(x);
  }
}
