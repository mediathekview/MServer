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

public class KikaLetterPageUrlTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.bundleNaviItem > a.pageItem";
  private final String baseUrl;

  public KikaLetterPageUrlTask(
      final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> urlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, urlToCrawlDtos, jsoupConnection);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    final Elements urlElements = aDocument.select(LETTER_PAGE_URL_SELECTOR);
    for (final Element urlElement : urlElements) {
      final String url = urlElement.attr(Consts.ATTRIBUTE_HREF);
      taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), aUrlDto.getFilmType()));
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aElementsToProcess) {
    return new KikaLetterPageUrlTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
