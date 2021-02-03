package mServer.crawler.sender.kika.tasks;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractDocumentTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.br.Consts;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto.FilmType;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.concurrent.ConcurrentLinkedQueue;

public class KikaTopicLandingPageTask extends AbstractDocumentTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> {
  // Button "Folgenübersicht" auch ohne "sectionArticleWrapperRight" (siehe tib und tum-tum)
  private static final String SELECTOR_TOPIC_OVERVIEW1 = "span.moreBtn > a";
  // Landingpage with "Alle Folgen"
  private static final String SELECTOR_TOPIC_OVERVIEW2 = "div.teaserMultiGroup > a.linkAll";

  private final String baseUrl;

  public KikaTopicLandingPageTask(
          final MediathekReader crawler,
          final ConcurrentLinkedQueue<KikaCrawlerUrlDto> urlToCrawlDtos,
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
      final String url = overviewUrlElement.attr(Consts.ATTRIBUTE_HREF);
      if (url.startsWith("http") || url.charAt(0) == '/') {
        taskResults.add(new KikaCrawlerUrlDto(UrlUtils.addDomainIfMissing(url, baseUrl), filmType));
      }
    }
  }

  @Override
  protected AbstractRecursivConverterTask<KikaCrawlerUrlDto, KikaCrawlerUrlDto> createNewOwnInstance(
          final ConcurrentLinkedQueue<KikaCrawlerUrlDto> aElementsToProcess) {
    return new KikaTopicLandingPageTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
