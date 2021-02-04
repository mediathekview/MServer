package mServer.crawler.sender.kika.tasks;

import mServer.crawler.sender.kika.KikaConstants;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

public final class KikaHelper {

  static Optional<String> gatherIpgTriggerUrlFromElement(
      final Element aUrlElement, final String attributeNameTrigger, final String baseUrl) {
    if (aUrlElement.hasAttr(attributeNameTrigger)) {
      final Matcher urlMatcher =
          Pattern.compile(KikaConstants.GATHER_URL_REGEX_PATTERN)
              .matcher(aUrlElement.attr(attributeNameTrigger));
      if (urlMatcher.find()) {
        return Optional.of(baseUrl + urlMatcher.group());
      }
    }
    return Optional.empty();
  }

  private KikaHelper() {}
}
