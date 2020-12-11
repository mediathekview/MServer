package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Queue;

public class KikaTopicLandingPageTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  // Button "Folgenübersicht" auch ohne "sectionArticleWrapperRight" (siehe tib und tum-tum)
  private static final String SELECTOR_TOPIC_OVERVIEW1 = "span.moreBtn > a";
  // Landingpage with "Alle Folgen"
  private static final String SELECTOR_TOPIC_OVERVIEW2 = "div.teaserMultiGroup > a.linkAll";

  private final String baseUrl;

  public KikaTopicLandingPageTask(
      final AbstractCrawler crawler,
      final Queue<CrawlerUrlDTO> urlToCrawlDTOs,
      final String baseUrl,
      final JsoupConnection jsoupConnection) {
    super(crawler, urlToCrawlDTOs, jsoupConnection);
    this.baseUrl = baseUrl;
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    Elements overviewUrlElementsMoteBtn = aDocument.select(SELECTOR_TOPIC_OVERVIEW1);
    parseOverviewLink(overviewUrlElementsMoteBtn);

    Elements overviewUrlElementsMultigroup = aDocument.select(SELECTOR_TOPIC_OVERVIEW2);
    parseOverviewLink(overviewUrlElementsMultigroup);
    
    // es ist eine Uebersichtseite (z.B.Schnitzeljadgt / Schloss Einstein) ohne "Alle Folgen" knopf
    if (overviewUrlElementsMoteBtn.size() == 0 && overviewUrlElementsMultigroup.size() == 0) {
      taskResults.add(aUrlDTO);
    }
  }

  private void parseOverviewLink(final Elements overviewUrlElements) {
    for (final Element overviewUrlElement : overviewUrlElements) {
      final String url = overviewUrlElement.attr(HtmlConsts.ATTRIBUTE_HREF);
      if (url.startsWith("http") || url.charAt(0) == '/') {
        taskResults.add(new CrawlerUrlDTO(UrlUtils.addDomainIfMissing(url, baseUrl)));
      }
    }
  }

  @Override
  protected AbstractRecursiveConverterTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new KikaTopicLandingPageTask(crawler, aElementsToProcess, baseUrl, getJsoupConnection());
  }
}
