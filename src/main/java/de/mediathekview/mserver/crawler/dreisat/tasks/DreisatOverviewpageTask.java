package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Crawles overview pages like "http://www.3sat.de/mediathek/?mode=verpasst" or
 * "http://www.3sat.de/mediathek/?mode=sendungenaz".
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Jabber:</b> nicklas2751@elaon.de<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public abstract class DreisatOverviewpageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(DreisatOverviewpageTask.class);
  private static final long serialVersionUID = -5344360936192332131L;
  private static final String ELEMENT_CLASS_SENDUNG_LINK = ".BoxHeadline .MediathekLink";
  private static final String ELEMENT_CLASS_LAST_SUBPAGE_LINK =
      ".mediathek_search_navi .ClnNextNblEnd";

  protected final boolean countMax;
  protected final int maxSubPages;

  public DreisatOverviewpageTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      final boolean aCountMax,
      final int aMaxSubpages) {
    super(aCrawler, aUrlToCrawlDTOs);
    countMax = aCountMax;
    maxSubPages = aMaxSubpages;
  }

  private Optional<Integer> getMaxSubpageNumber(final Document aDocument) {
    final Elements lastSubpageLinkElements = aDocument.select(ELEMENT_CLASS_LAST_SUBPAGE_LINK);
    if (!lastSubpageLinkElements.isEmpty()) {
      if (lastSubpageLinkElements.hasAttr(Consts.ATTRIBUTE_HREF)) {
        final String lastSubpageLink = lastSubpageLinkElements.attr(Consts.ATTRIBUTE_HREF);
        final Matcher maxSubpageNumberMatcher = getReqExMaxSubPageNumber().matcher(lastSubpageLink);
        if (maxSubpageNumberMatcher.find()) {
          final String maxSubpageNumberText = maxSubpageNumberMatcher.group();
          try {
            return Optional.of(Integer.parseInt(maxSubpageNumberText));
          } catch (final NumberFormatException numberFormatException) {
            LOG.debug(
                String.format(
                    "A subpage number isn't a valid number: \"%s\"", maxSubpageNumberText),
                numberFormatException);
          }
        }
      }
    }
    return Optional.empty();
  }

  private Optional<AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO>> processSubpages(
      final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    final Optional<Integer> maxSubpageNumber = getMaxSubpageNumber(aDocument);
    if (maxSubpageNumber.isPresent()) {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> subpageUrls = new ConcurrentLinkedQueue<>();
      for (int i = 1; i <= maxSubpageNumber.get() && i < maxSubPages; i++) {

        subpageUrls.add(
            new CrawlerUrlDTO(aUrlDTO.getUrl() + String.format(getSubPageLinkExtension(), i)));
      }
      final AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> subpageCrawler =
          createNewOwnInstance(subpageUrls);
      subpageCrawler.fork();
      return Optional.of(subpageCrawler);
    }
    return Optional.empty();
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    Optional<AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO>> subpageCrawler;
    if (aUrlDTO.getUrl().matches(getSubpageExistRegex())) {
      subpageCrawler = Optional.empty();
    } else {
      subpageCrawler = processSubpages(aUrlDTO, aDocument);
    }

    final Elements sendungLinkElements = aDocument.select(ELEMENT_CLASS_SENDUNG_LINK);
    for (final Element sendungLinkElement : sendungLinkElements) {
      if (sendungLinkElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlDTO(sendungLinkElement.attr(Consts.ATTRIBUTE_HREF)));
        if (countMax) {
          crawler.incrementAndGetMaxCount();
          crawler.updateProgress();
        }
      }
    }

    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }
  }

  @Override
  protected abstract AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl);

  protected abstract Pattern getReqExMaxSubPageNumber();

  protected abstract String getSubPageLinkExtension();

  protected abstract String getSubpageExistRegex();
}
