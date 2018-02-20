package de.mediathekview.mserver.crawler.wdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDTO;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WdrLetterPageDeserializer extends WdrLetterPageDeserializerBase {
  private static final Logger LOG = LogManager.getLogger(WdrLetterPageDeserializer.class);
  
  private static final String SELECTOR_TOPIC = "ul.list > li > a";
  private static final String SELECTOR_TITLE = "span";
  
  public List<WdrTopicUrlDTO> deserialize(final Document aDocument) {
    List<WdrTopicUrlDTO> results = new ArrayList<>();

    Elements topics = aDocument.select(SELECTOR_TOPIC);
    topics.forEach(topicElement -> {
      String url = getUrl(topicElement);
      if (!url.isEmpty()) {
        String topic = getTopic(topicElement);
        boolean isFileUrl = isFileUrl(topicElement, true);
        results.add(new WdrTopicUrlDTO(topic, url, isFileUrl));
      }
    });
    return results;
  }
  
  private String getTopic(Element aTopicElement) {

    Element titleElement = aTopicElement.select(SELECTOR_TITLE).first();
    if(titleElement != null) {
        return titleElement.text();
    }

    LOG.debug("WdrLetterPageDeserializer: no topic found.");
    return "";
  }

  private String getUrl(Element aTopicElement) {
      String url = aTopicElement.attr(ATTRIBUTE_HREF);

      if(!url.isEmpty()) {
        url = UrlUtils.addDomainIfMissing(url, WdrConstants.URL_BASE);
      }

      return url;
  }  
}
