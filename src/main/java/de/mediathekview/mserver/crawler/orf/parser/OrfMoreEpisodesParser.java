package de.mediathekview.mserver.crawler.orf.parser;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;

public class OrfMoreEpisodesParser {
  private static final String EPISODES_SELECTOR = "article.b-teaser > a.teaser-link";

  public List<TopicUrlDTO> parse(final Document document, final String topic) {
    final List<TopicUrlDTO> result = new ArrayList<>();

    document
        .select(EPISODES_SELECTOR)
        .forEach(
            episode -> {
              final String url = episode.attr(HtmlConsts.ATTRIBUTE_HREF);
              result.add(new TopicUrlDTO(topic, url));
            });

    return result;
  }
}
