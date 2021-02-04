package mServer.crawler.sender.kika.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.br.Consts;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaLetterPageTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {

  // siehe Sonntagsmaerchen
  private static final String TOPIC_URL_SELECTOR = "div.teaserStandard a.linkAll";
  private final String baseUrl;

  public KikaLetterPageTask(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aUrlToCrawlDtos,
          final String aBaseUrl,
          final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    final Elements topicUrlElements = aDocument.select(TOPIC_URL_SELECTOR);
    for (final Element topicUrlElement : topicUrlElements) {
      final String url = topicUrlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), aUrlDto.getFilmType()));
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
          final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aElementsToProcess) {
    return new KikaLetterPageTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
