package mServer.crawler.sender.kika.tasks;

import de.mediathekview.mlib.tool.Log;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;
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
      final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlToCrawlDtos,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
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
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlsToCrawl) {
    return new KikaSendungsfolgeVideoUrlTask(crawler, aUrlsToCrawl, getJsoupConnection());
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
          Log.sysLog("missing element data url");
        }
      } else {
        Log.sysLog("missing element onclick");
      }
    }
  }
}
