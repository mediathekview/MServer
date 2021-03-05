package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;

public class KikaLetterPageUrlTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.bundleNaviItem > a.pageItem";
  private final String baseUrl;

  public KikaLetterPageUrlTask(
      final AbstractCrawler aCrawler,
      final Queue<KikaCrawlerUrlDto> urlToCrawlDtos,
      final String aBaseUrl) {
    super(aCrawler, urlToCrawlDtos);
    baseUrl = aBaseUrl;
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    final Elements urlElements = aDocument.select(LETTER_PAGE_URL_SELECTOR);
    for (final Element urlElement : urlElements) {
      final String url = urlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), aUrlDto.getFilmType()));
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final Queue<KikaCrawlerUrlDto> aElementsToProcess) {
    return new KikaLetterPageUrlTask(crawler, aElementsToProcess, baseUrl);
  }
}
