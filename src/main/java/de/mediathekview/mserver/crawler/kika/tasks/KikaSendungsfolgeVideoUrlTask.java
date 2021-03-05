package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KikaSendungsfolgeVideoUrlTask
    extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {
  private static final String URL_TEMPLATE = "https://www.kika.de%s";
  private static final String HTTP = "http";
  private static final String ATTRIBUTE_ONCLICK = "onclick";
  private static final long serialVersionUID = -2633978090540666539L;
  private static final String VIDEO_DATA_ELEMENT_SELECTOR =
      ".sectionArticle .av-playerContainer a[onclick]";
  private static final String VIDEO_URL_REGEX_PATTERN = "(?<=dataURL:')[^']*";

  public KikaSendungsfolgeVideoUrlTask(
      final AbstractCrawler aCrawler,
      final Queue<KikaCrawlerUrlDto> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos);
  }

  private String toKikaUrl(final String aUrl) {
    final String kikaUrl;
    if (aUrl.contains(HTTP)) {
      kikaUrl = aUrl;
    } else {
      kikaUrl = String.format(URL_TEMPLATE, aUrl);
    }
    return kikaUrl;
  }

  @Override
  protected AbstractUrlTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final Queue<KikaCrawlerUrlDto> aUrlsToCrawl) {
    return new KikaSendungsfolgeVideoUrlTask(crawler, aUrlsToCrawl);
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    final Elements videoElements = aDocument.select(VIDEO_DATA_ELEMENT_SELECTOR);
    for (final Element videoDataElement : videoElements) {
      if (videoDataElement.hasAttr(ATTRIBUTE_ONCLICK)) {
        final String rawVideoData = videoDataElement.attr(ATTRIBUTE_ONCLICK);
        final Matcher videoUrlMatcher =
            Pattern.compile(VIDEO_URL_REGEX_PATTERN).matcher(rawVideoData);
        if (videoUrlMatcher.find()) {
          taskResults.add(new KikaCrawlerUrlDto(toKikaUrl(videoUrlMatcher.group()), aUrlDto.getFilmType()));
        } else {
          crawler.printMissingElementErrorMessage("data url");
        }
      } else {
        crawler.printMissingElementErrorMessage(ATTRIBUTE_ONCLICK);
      }
    }
  }
}
