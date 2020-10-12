package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.phoenix.PhoenixConstants;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDeserializer;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDto;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmXmlHandler;
import de.mediathekview.mserver.crawler.zdf.DownloadDtoFilmConverter;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.annotation.Nullable;
import javax.ws.rs.client.WebTarget;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class PhoenixFilmDetailTask extends ZdfTaskBase<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(PhoenixFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN =
      new TypeToken<Optional<PhoenixFilmDetailDto>>() {}.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN =
      new TypeToken<Optional<DownloadDto>>() {}.getType();

  private final String filmDetailHost;
  private final String videoDetailHost;

  private final transient ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();

  public PhoenixFilmDetailTask(
      final AbstractCrawler aCrawler,
      final Queue<CrawlerUrlDTO> aUrlToCrawlDTOs,
      @Nullable final String authKey,
      final String filmDetailHost,
      final String videoDetailHost) {
    super(aCrawler, aUrlToCrawlDTOs, authKey);
    this.filmDetailHost = filmDetailHost;
    this.videoDetailHost = videoDetailHost;

    registerJsonDeserializer(
        OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new PhoenixFilmDetailDeserializer());
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      final Queue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixFilmDetailTask(
        crawler, aElementsToProcess, getAuthKey().orElse(null), filmDetailHost, videoDetailHost);
  }

  @Override
  protected void processRestTarget(final CrawlerUrlDTO aDTO, final WebTarget aTarget) {
    final Optional<PhoenixFilmDetailDto> filmDetailDtoOptional =
        deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    if (filmDetailDtoOptional.isEmpty()) {
      // tritt auf, wenn kein Film vorhanden
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final PhoenixFilmDetailDto filmDetailDto = filmDetailDtoOptional.get();
    final Optional<PhoenixFilmXmlHandler> filmXmlDtoOptional =
        loadFilmXml(filmDetailDto.getBaseName());
    if (filmXmlDtoOptional.isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final PhoenixFilmXmlHandler filmXmlHandler = filmXmlDtoOptional.get();
    if (filmXmlHandler.getBaseName() == null) {
      // tritt auf, wenn kein Film vorhanden
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    final Optional<DownloadDto> videoDetailDtoOptional =
        deserializeOptional(
            createWebTarget(
                videoDetailHost
                    + PhoenixConstants.URL_VIDEO_DETAILS_BASE
                    + filmXmlHandler.getBaseName()),
            OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);
    if (videoDetailDtoOptional.isEmpty()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    try {
      addFilm(filmDetailDto, filmXmlHandler, videoDetailDtoOptional.get());
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    } catch (final MalformedURLException e) {
      LOG.error("PhoenixFilmDetailTask: url can't be parsed: ", e);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  private void addFilm(
      final PhoenixFilmDetailDto filmDetailDto,
      final PhoenixFilmXmlHandler filmXmlHandler,
      final DownloadDto downloadDto)
      throws MalformedURLException {
    final Film film =
        new Film(
            UUID.randomUUID(),
            Sender.PHOENIX,
            filmDetailDto.getTitle(),
            filmDetailDto.getTopic(),
            filmXmlHandler.getTime(),
            filmXmlHandler.getDuration());

    film.setBeschreibung(filmDetailDto.getDescription());

    if (filmDetailDto.getWebsite().isPresent()) {
      try {
        film.setWebsite(new URL(filmDetailDto.getWebsite().get()));
      } catch (final MalformedURLException ex) {
        LOG.error(
            String.format("A website URL \"%s\" isn't valid.", filmDetailDto.getWebsite()), ex);
      }
    }

    DownloadDtoFilmConverter.addUrlsToFilm(
        film, downloadDto, Optional.of(optimizer), ZdfConstants.LANGUAGE_GERMAN);
    taskResults.add(film);
  }

  private Optional<PhoenixFilmXmlHandler> loadFilmXml(final String baseName) {
    final String xmlUrl = filmDetailHost + PhoenixConstants.URL_FILM_DETAIL_XML + baseName;

    try {

      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      saxParser.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      final PhoenixFilmXmlHandler handler = new PhoenixFilmXmlHandler();
      saxParser.parse(xmlUrl, handler);

      return Optional.of(handler);

    } catch (final SAXException | IOException | ParserConfigurationException e) {
      LOG.error(String.format("Error loading xml document \"%s\".", xmlUrl), e);
    }

    return Optional.empty();
  }
}
