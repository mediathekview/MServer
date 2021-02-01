package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;

public class KikaTopicLandingPageTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {
  // Button "Folgenübersicht" auch ohne "sectionArticleWrapperRight" (siehe tib und tum-tum)
  private static final String SELECTOR_TOPIC_OVERVIEW1 = "span.moreBtn > a";
  // Landingpage with "Alle Folgen"
  private static final String SELECTOR_TOPIC_OVERVIEW2 = "div.teaserMultiGroup > a.linkAll";

  private final String baseUrl;

  public KikaTopicLandingPageTask(
      final AbstractCrawler crawler,
      final Queue<KikaCrawlerUrlDto> urlToCrawlDtos,
      final String baseUrl,
      final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDtos, jsoupConnection);
    this.baseUrl = baseUrl;
  }

  @Override
  protected void processDocument(final KikaCrawlerUrlDto aUrlDto, final Document aDocument) {
    Elements overviewUrlElementsMoteBtn = aDocument.select(SELECTOR_TOPIC_OVERVIEW1);
    parseOverviewLink(overviewUrlElementsMoteBtn, aUrlDto.getFilmType());

    Elements overviewUrlElementsMultigroup = aDocument.select(SELECTOR_TOPIC_OVERVIEW2);
    parseOverviewLink(overviewUrlElementsMultigroup, aUrlDto.getFilmType());
    
    // es ist eine Uebersichtseite (z.B.Schnitzeljadgt / Schloss Einstein) ohne "Alle Folgen" knopf
    if (overviewUrlElementsMoteBtn.isEmpty() && overviewUrlElementsMultigroup.isEmpty()) {
      taskResults.add(aUrlDto);
    }
  }

  private void parseOverviewLink(final Elements overviewUrlElements, FilmType filmType) {
    for (final Element overviewUrlElement : overviewUrlElements) {
      final String url = overviewUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      if (url.startsWith("http") || url.charAt(0) == '/') {
        taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), filmType));
      }
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
      final Queue<KikaCrawlerUrlDto> aElementsToProcess) {
    return new KikaTopicLandingPageTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
