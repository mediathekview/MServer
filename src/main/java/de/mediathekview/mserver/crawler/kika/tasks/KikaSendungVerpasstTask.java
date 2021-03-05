package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaSendungVerpasstTask extends AbstractDocumentTask<KikaCrawlerUrlDto, CrawlerUrlDTO> {
  private static final long serialVersionUID = -6483678632833327433L;
  private static final String PAGE_ANKER = "#";
  private static final String URL_SELECTOR = ".hasAvContent.programEntry .linkAll";
  private static final String ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER = "data-ctrl-ipg-flexloadtrigger";
  private static final String SELECTOR_FLEX_LOAD = ".flexloadTrigger";
  private final String baseUrl;

  public KikaSendungVerpasstTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDtos);
    baseUrl = aBaseUrl;
  }

  @Override
  protected AbstractUrlTask<KikaCrawlerUrlDto, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aUrlsToCrawl) {
    return new KikaSendungVerpasstTask(crawler, aUrlsToCrawl, baseUrl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {
    parseFilmUrls(aDocument, FilmType.NORMAL);

    final Queue<CrawlerUrlDTO> flexLoadUrls = parseFlexLoad(aDocument);
    if (!flexLoadUrls.isEmpty()) {
      taskResults.addAll(createNewOwnInstance(flexLoadUrls).invoke());
    }
  }

  private void parseFilmUrls(final Document aDocument, FilmType filmType) {
    for (final Element filmUrlElement : aDocument.select(URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(HtmlConsts.ATTRIBUTE_HREF)
          && !PAGE_ANKER.equals(filmUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF))) {

        final String url =
            UrlUtils.addDomainIfMissing(
                filmUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF), KikaConstants.BASE_URL);
        taskResults.add(new KikaCrawlerUrlDto(url, filmType));
      }
    }
  }

  private Queue<CrawlerUrlDTO> parseFlexLoad(final Document aDocument) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (final Element flexLoadElement : aDocument.select(SELECTOR_FLEX_LOAD)) {
      final Optional<String> url =
          KikaHelper.gatherIpgTriggerUrlFromElement(
              flexLoadElement, ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER, baseUrl);
      url.ifPresent(s -> urls.add(new CrawlerUrlDTO(s)));
    }

    return urls;
  }
}
