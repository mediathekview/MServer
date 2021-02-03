package mServer.crawler.sender.kika.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.*;
import mServer.crawler.sender.br.Consts;
import mServer.crawler.sender.kika.KikaConstants;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto.FilmType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaSendungVerpasstTask extends AbstractDocumentTask<KikaCrawlerUrlDto, CrawlerUrlDTO> {
  private static final long serialVersionUID = -6483678632833327433L;
  private static final String PAGE_ANKER = "#";
  private static final String URL_SELECTOR = ".hasAvContent.programEntry .linkAll";
  private static final String ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER = "data-ctrl-ipg-flexloadtrigger";
  private static final String SELECTOR_FLEX_LOAD = ".flexloadTrigger";
  private final String baseUrl;

  public KikaSendungVerpasstTask(
      final MediathekReader aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl,
      final JsoupConnection jsoupConnection) {
    super(aCrawler, aUrlToCrawlDtos, jsoupConnection);
    baseUrl = aBaseUrl;
  }

  @Override
  protected AbstractUrlTask<KikaCrawlerUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new KikaSendungVerpasstTask(crawler, aUrlsToCrawl, baseUrl, getJsoupConnection());
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    parseFilmUrls(aDocument, FilmType.NORMAL);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> flexLoadUrls = parseFlexLoad(aDocument);
    if (!flexLoadUrls.isEmpty()) {
      taskResults.addAll(createNewOwnInstance(flexLoadUrls).invoke());
    }
  }

  private void parseFilmUrls(final Document aDocument, FilmType filmType) {
    for (final Element filmUrlElement : aDocument.select(URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)
          && !PAGE_ANKER.equals(filmUrlElement.attr(Consts.ATTRIBUTE_HREF))) {

        final String url =
            UrlUtils.addDomainIfMissing(
                filmUrlElement.attr(Consts.ATTRIBUTE_HREF), KikaConstants.BASE_URL);
        taskResults.add(new KikaCrawlerUrlDto(url, filmType));
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> parseFlexLoad(final Document aDocument) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (final Element flexLoadElement : aDocument.select(SELECTOR_FLEX_LOAD)) {
      final Optional<String> url =
          KikaHelper.gatherIpgTriggerUrlFromElement(
              flexLoadElement, ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER, baseUrl);
      url.ifPresent(s -> urls.add(new CrawlerUrlDTO(s)));
    }

    return urls;
  }
}
