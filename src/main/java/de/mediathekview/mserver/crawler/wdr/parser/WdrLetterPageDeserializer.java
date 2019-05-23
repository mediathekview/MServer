package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class WdrLetterPageDeserializer extends WdrLetterPageDeserializerBase {
  private static final Logger LOG = LogManager.getLogger(WdrLetterPageDeserializer.class);

  private static final String SELECTOR_TOPIC = "ul.list > li > a";
  private static final String SELECTOR_TITLE = "span";

  public List<WdrTopicUrlDto> deserialize(final Document aDocument) {
    final List<WdrTopicUrlDto> results = new ArrayList<>();

    final Elements topics = aDocument.select(SELECTOR_TOPIC);
    topics.forEach(
        topicElement -> {
          final String url = getUrl(topicElement);
          if (!url.isEmpty()) {
            final String topic = getTopic(topicElement);
            final boolean isFileUrl = isFileUrl(topicElement, true);
            results.add(new WdrTopicUrlDto(topic, url, isFileUrl));
          }
        });
    return results;
  }

  private String getTopic(final Element aTopicElement) {

    final Element titleElement = aTopicElement.select(SELECTOR_TITLE).first();
    if (titleElement != null) {
      return titleElement.text();
    }

    LOG.debug("WdrLetterPageDeserializer: no topic found.");
    return "";
  }

  private String getUrl(final Element aTopicElement) {
    String url = aTopicElement.attr(ATTRIBUTE_HREF);

    if (!url.isEmpty()) {
      url = UrlUtils.addDomainIfMissing(url, WdrConstants.URL_BASE);
    }

    return url;
  }
}
