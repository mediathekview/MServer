package de.mediathekview.mserver.crawler.dreisat.tasks;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlsDTO;

/**
 * Crawles overview pages like "http://www.3sat.de/mediathek/?mode=verpasst" or
 * "http://www.3sat.de/mediathek/?mode=sendungenaz".
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public class DreisatOverviewpageTask extends AbstractUrlTask<CrawlerUrlsDTO, CrawlerUrlsDTO> {
  private static final Logger LOG = LogManager.getLogger(DreisatOverviewpageTask.class);
  private static final long serialVersionUID = -5344360936192332131L;
  private static final String ELEMENT_CLASS_SENDUNG_LINK = ".BoxHeadline .MediathekLink";
  private static final String ELEMENT_CLASS_LAST_SUBPAGE_LINK =
      ".mediathek_search_navi .ClnNextNblEnd";
  private static final String SUBPAGE_LINK_EXTENSION = "&mode=verpasst";
  private static final String SUBPAGE_LINK_EXTENSION_TEMPLATE = SUBPAGE_LINK_EXTENSION + "%d";
  private static final String REGEX_MAX_SUBPAGE_NUMBER = "(?<=verpasst)\\d+";
  private final boolean countMax;
  private final int maxSubpages;

  public DreisatOverviewpageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlsDTO> aUrlToCrawlDTOs, final boolean aCountMax,
      final int aMaxSubpages) {
    super(aCrawler, aUrlToCrawlDTOs);
    countMax = aCountMax;
    maxSubpages = aMaxSubpages;
  }

  private Optional<Integer> getMaxSubpageNumber(final Document aDocument) {
    final Elements lastSubpageLinkElements =
        aDocument.getElementsByClass(ELEMENT_CLASS_LAST_SUBPAGE_LINK);
    if (!lastSubpageLinkElements.isEmpty()) {
      if (lastSubpageLinkElements.hasAttr(Consts.ATTRIBUTE_HREF)) {
        final String lastSubpageLink = lastSubpageLinkElements.attr(Consts.ATTRIBUTE_HREF);
        final Matcher maxSubpageNumberMatcher =
            Pattern.compile(REGEX_MAX_SUBPAGE_NUMBER).matcher(lastSubpageLink);
        if (maxSubpageNumberMatcher.find()) {
          final String maxSubpageNumberText = maxSubpageNumberMatcher.group();
          try {
            return Optional.of(Integer.parseInt(maxSubpageNumberText));
          } catch (final NumberFormatException numberFormatException) {
            LOG.debug(String.format("A subpage number isn't a valid number: \"%s\"",
                maxSubpageNumberText), numberFormatException);
          }
        }
      }
    }
    return Optional.empty();
  }

  private Optional<AbstractUrlTask<CrawlerUrlsDTO, CrawlerUrlsDTO>> processSubpages(
      final CrawlerUrlsDTO aUrlDTO, final Document aDocument) {
    final Optional<Integer> maxSubpageNumber = getMaxSubpageNumber(aDocument);
    if (maxSubpageNumber.isPresent()) {
      final ConcurrentLinkedQueue<CrawlerUrlsDTO> subpageUrls = new ConcurrentLinkedQueue<>();
      for (int i = 1; i < maxSubpageNumber.get() && i < maxSubpages; i++) {
        subpageUrls.add(new CrawlerUrlsDTO(
            aUrlDTO.getUrl() + String.format(SUBPAGE_LINK_EXTENSION_TEMPLATE, i)));
      }
      final AbstractUrlTask<CrawlerUrlsDTO, CrawlerUrlsDTO> subpageCrawler =
          createNewOwnInstance(subpageUrls);
      subpageCrawler.fork();
      return Optional.of(subpageCrawler);
    }
    return Optional.empty();
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlsDTO, CrawlerUrlsDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlsDTO> aURLsToCrawl) {
    return new DreisatOverviewpageTask(crawler, aURLsToCrawl, countMax, maxSubpages);
  }

  @Override
  protected void processDocument(final CrawlerUrlsDTO aUrlDTO, final Document aDocument) {
    Optional<AbstractUrlTask<CrawlerUrlsDTO, CrawlerUrlsDTO>> subpageCrawler;
    if (aUrlDTO.getUrl().matches(".*" + SUBPAGE_LINK_EXTENSION_TEMPLATE + ".*")) {
      subpageCrawler = Optional.empty();
    } else {
      subpageCrawler = processSubpages(aUrlDTO, aDocument);
    }

    final Elements sendungLinkElements = aDocument.getElementsByClass(ELEMENT_CLASS_SENDUNG_LINK);
    for (final Element sendungLinkElement : sendungLinkElements) {
      if (sendungLinkElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlsDTO(sendungLinkElement.attr(Consts.ATTRIBUTE_HREF)));
        if (countMax) {
          crawler.incrementAndGetMaxCount();
        }
      }
    }

    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }

  }

}
