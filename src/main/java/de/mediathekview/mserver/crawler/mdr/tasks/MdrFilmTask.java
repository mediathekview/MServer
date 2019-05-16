package de.mediathekview.mserver.crawler.mdr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.parser.MdrFilmPageDeserializer;
import de.mediathekview.mserver.crawler.mdr.parser.MdrFilmXmlHandler;
import mServer.crawler.CrawlerTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MdrFilmTask extends AbstractDocumentTask<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(MdrFilmTask.class);

  private final String baseUrl;
  private final MdrFilmPageDeserializer filmPageDeserializer;

  public MdrFilmTask(
      final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDtos,
      final String aBaseUrl) {
    super(aCrawler, aUrlToCrawlDtos);

    baseUrl = aBaseUrl;
    filmPageDeserializer = new MdrFilmPageDeserializer(baseUrl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDto, final Document aDocument) {

    final Set<CrawlerUrlDTO> filmEntries = filmPageDeserializer.deserialize(aDocument);
    if (filmEntries.size() > 1) {
      // The max count equals the count of urls given to this task. So if this task creates more
      // then one film of a url we need to actualize the max count.
      crawler.incrementMaxCountBySizeAndGetNewSize(filmEntries.size() - 1);
    }

    for (final CrawlerUrlDTO filmEntry : filmEntries) {
      try {
        final Optional<MdrFilmXmlHandler> mdrFilmXmlHandler = loadFilmXml(filmEntry);
        if (mdrFilmXmlHandler.isPresent()) {
          final Film film = createFilm(mdrFilmXmlHandler.get());
          taskResults.add(film);
          crawler.incrementAndGetActualCount();
          crawler.updateProgress();
        } else {
          LOG.error("MdrFilmTask: no film found for url " + aUrlDto.getUrl());
          crawler.incrementAndGetErrorCount();
          crawler.updateProgress();
        }
      } catch (final MalformedURLException e) {
        LOG.error("MdrFilmTask: error reading video infos " + aUrlDto.getUrl(), e);
        crawler.incrementAndGetErrorCount();
        crawler.updateProgress();
      }
    }
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new MdrFilmTask(crawler, aElementsToProcess, baseUrl);
  }

  private Optional<MdrFilmXmlHandler> loadFilmXml(final CrawlerUrlDTO aXmlUrl) {
    try {

      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      final MdrFilmXmlHandler handler = new MdrFilmXmlHandler();
      saxParser.parse(aXmlUrl.getUrl(), handler);

      return Optional.of(handler);

    } catch (final SAXException | IOException | ParserConfigurationException e) {
      LOG.error(String.format("Error loading xml document \"%s\".", aXmlUrl.getUrl()), e);
    }

    return Optional.empty();
  }

  private Film createFilm(final MdrFilmXmlHandler aFilmXmlHandler) throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.MDR,
            aFilmXmlHandler.getTitle(),
            aFilmXmlHandler.getTopic(),
            aFilmXmlHandler.getTime(),
            aFilmXmlHandler.getDuration());

    film.setBeschreibung(aFilmXmlHandler.getDescription());
    try {
      film.setGeoLocations(
          CrawlerTool.getGeoLocations(Sender.MDR, aFilmXmlHandler.getVideoUrl(Resolution.NORMAL)));
    } catch (final NullPointerException e) {
      LOG.error(e);
    }
    film.setWebsite(new URL(aFilmXmlHandler.getWebsite()));
    if (StringUtils.isNotBlank(aFilmXmlHandler.getSubtitle())) {
      film.addSubtitle(new URL(aFilmXmlHandler.getSubtitle()));
    }

    addUrl(Resolution.SMALL, film, aFilmXmlHandler);
    addUrl(Resolution.NORMAL, film, aFilmXmlHandler);
    addUrl(Resolution.HD, film, aFilmXmlHandler);

    return film;
  }

  private void addUrl(
      final Resolution aResolution, final Film aFilm, final MdrFilmXmlHandler aFilmXmlHandler)
      throws MalformedURLException {
    final String videoUrl = aFilmXmlHandler.getVideoUrl(aResolution);
    if (StringUtils.isNotBlank(videoUrl)) {
      aFilm.addUrl(aResolution, CrawlerTool.stringToFilmUrl(videoUrl));
    }
  }
}
