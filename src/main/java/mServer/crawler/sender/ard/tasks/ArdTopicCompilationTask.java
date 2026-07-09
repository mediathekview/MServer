package mServer.crawler.sender.ard.tasks;

import com.google.gson.reflect.TypeToken;
import jakarta.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.ard.ArdTopicInfoDto;
import mServer.crawler.sender.ard.json.ArdTopicCompilationDeserializer;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArdTopicCompilationTask extends ArdTaskBase<ArdFilmInfoDto, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(ArdTopicCompilationTask.class);
  private static final Pattern UUID_PATTERN = Pattern.compile("/compilation/([^/?]+)([/?])");
  private static final Pattern WIDGETS_SENDER_PATTERN = Pattern.compile("/pages/([^/]+)/"); // captures the segment after /widgets/ (e.g. "ard")

  private static final Type ARDTOPICINFODTO_TYPE_TOKEN =
          new TypeToken<ArdTopicInfoDto>() {
          }.getType();

  public ArdTopicCompilationTask(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);

    registerJsonDeserializer(ARDTOPICINFODTO_TYPE_TOKEN, new ArdTopicCompilationDeserializer());
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final ArdTopicInfoDto topicInfo = deserialize(aTarget, ARDTOPICINFODTO_TYPE_TOKEN);
    if (topicInfo != null
            && topicInfo.getFilmInfos() != null
            && !topicInfo.getFilmInfos().isEmpty()) {
      taskResults.addAll(topicInfo.getFilmInfos());

      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = createSubPageUrls(aDTO, topicInfo);
      if (!subpages.isEmpty()) {
        taskResults.addAll(createNewOwnInstance(subpages).fork().join());
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createSubPageUrls(CrawlerUrlDTO urlDto, ArdTopicInfoDto topicInfo) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> subpages = new ConcurrentLinkedQueue<>();
    if (topicInfo.getTotalElements() < topicInfo.getPageSize() || topicInfo.getPageNumber() > 0) {
      return subpages;
    }
    //
    final String sender = extractSender(urlDto.getUrl());
    final String id = extractCompilation(urlDto.getUrl());

    int index = 0;
    while ((topicInfo.getPageSize() + (index * topicInfo.getPageSize())) < topicInfo.getTotalElements()) {
      subpages.add(new CrawlerUrlDTO(String.format(ArdConstants.TOPIC_COMPILATION_PAGE_URL, sender, id, index + 1, topicInfo.getPageSize())));
      index++;
      if (index >= ArdConstants.MAX_COMPILATION_SUB_PAGES) {
        LOG.debug("ignore more subpage due to limit of {} pages but found {}", ArdConstants.MAX_COMPILATION_SUB_PAGES, Integer.valueOf(topicInfo.getTotalElements() / topicInfo.getPageSize()));
        break;
      }
    }
    return subpages;
  }

  private String extractCompilation(String url) {
    final Matcher m = UUID_PATTERN.matcher(url);
    if (m.find()) {
      return m.group(1);
    }

    LOG.warn("no compilation pattern found in url {}", url);
    return "";
  }

  private String extractSender(String url) {
    final Matcher m = WIDGETS_SENDER_PATTERN.matcher(url);
    if (m.find()) {
      return m.group(1);
    }
    LOG.warn("no sender pattern found in url {}", url);
    return "";
  }

  @Override
  protected AbstractRecursivConverterTask<ArdFilmInfoDto, CrawlerUrlDTO> createNewOwnInstance(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new ArdTopicCompilationTask(crawler, aElementsToProcess);
  }
}