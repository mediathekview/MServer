package de.mediathekview.mserver.crawler.zdf.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.zdf.json.ZdfLetterPageDeserializer;
import jakarta.ws.rs.client.WebTarget;
import java.lang.reflect.Type;
import java.util.Queue;
import java.util.Set;

public class ZdfLetterPageTask extends ZdfTaskBase<TopicUrlDTO, CrawlerUrlDTO> {

  private static final Type SET_TOPICURL_TYPE_TOKEN =
      new TypeToken<Set<TopicUrlDTO>>() {}.getType();

  public ZdfLetterPageTask(
          AbstractCrawler aCrawler, Queue<CrawlerUrlDTO> aUrlToCrawlDtos, String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    registerJsonDeserializer(SET_TOPICURL_TYPE_TOKEN, new ZdfLetterPageDeserializer());
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    final Set<TopicUrlDTO> actual = deserialize(aTarget, SET_TOPICURL_TYPE_TOKEN);
    if (actual != null) {
      taskResults.addAll(actual);
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<TopicUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new ZdfLetterPageTask(crawler, aElementsToProcess, getAuthKey().orElse(null));
  }
}
