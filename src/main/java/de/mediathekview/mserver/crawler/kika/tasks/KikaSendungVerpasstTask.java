package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class KikaSendungVerpasstTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = -6483678632833327433L;
  private static final String PAGE_ANKER = "#";
  private static final String URL_SELECTOR = ".hasAvContent.programEntry .linkAll";
  private static final String ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER = "data-ctrl-ipg-flexloadtrigger";
  private static final String SELECTOR_FLEX_LOAD = ".flexloadTrigger";
  private final String baseUrl;

  public KikaSendungVerpasstTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDTOs);
    baseUrl = aBaseUrl;
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungVerpasstTask(crawler, aURLsToCrawl, baseUrl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    parseFilmUrls(aDocument);

    ConcurrentLinkedQueue<CrawlerUrlDTO> flexLoadUrls = parseFlexLoad(aDocument);
    if (!flexLoadUrls.isEmpty()) {
      taskResults.addAll(createNewOwnInstance(flexLoadUrls).invoke());
    }
  }

  private void parseFilmUrls(Document aDocument) {
    for (final Element filmUrlElement : aDocument.select(URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)
          && !PAGE_ANKER.equals(filmUrlElement.attr(Consts.ATTRIBUTE_HREF))) {

        final String url =
            UrlUtils.addDomainIfMissing(
                filmUrlElement.attr(Consts.ATTRIBUTE_HREF), KikaConstants.BASE_URL);
        taskResults.add(new CrawlerUrlDTO(url));
      }
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> parseFlexLoad(Document aDocument) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (final Element flexLoadElement : aDocument.select(SELECTOR_FLEX_LOAD)) {
      Optional<String> url =
          KikaHelper.gatherIpgTriggerUrlFromElement(
              flexLoadElement, ATTRIBUTE_IPG_FLEX_LOAD_TRIGGER, baseUrl);
      if (url.isPresent()) {
        urls.add(new CrawlerUrlDTO(url.get()));
      }
    }

    return urls;
  }
}
