package mServer.crawler.sender.orf.parser;


import mServer.crawler.sender.orf.TopicUrlDTO;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class OrfMoreEpisodesParser {
  private static final String EPISODES_SELECTOR = "article.b-teaser > a.teaser-link";
  private static final String ATTRIBUTE_HREF = "href";

  public List<TopicUrlDTO> parse(final Document document, final String topic) {
    final List<TopicUrlDTO> result = new ArrayList<>();

    document
            .select(EPISODES_SELECTOR)
            .forEach(
                    episode -> {
                      final String url = episode.attr(ATTRIBUTE_HREF);
                      result.add(new TopicUrlDTO(topic, url));
                    });

    return result;
  }
}