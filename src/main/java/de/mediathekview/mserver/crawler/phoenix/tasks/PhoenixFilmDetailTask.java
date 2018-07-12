package de.mediathekview.mserver.crawler.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.phoenix.PhoenixConstants;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDeserializer;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmDetailDto;
import de.mediathekview.mserver.crawler.phoenix.parser.PhoenixFilmXmlHandler;
import de.mediathekview.mserver.crawler.zdf.DownloadDtoFilmConverter;
import de.mediathekview.mserver.crawler.zdf.ZdfVideoUrlOptimizer;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDto;
import de.mediathekview.mserver.crawler.zdf.json.ZdfDownloadDtoDeserializer;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTaskBase;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

public class PhoenixFilmDetailTask extends ZdfTaskBase<Film, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(PhoenixFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN = new TypeToken<Optional<PhoenixFilmDetailDto>>() {
  }.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN = new TypeToken<Optional<DownloadDto>>() {
  }.getType();

  private final String filmDetailHost;
  private final String videoDetailHost;

  private final ZdfVideoUrlOptimizer optimizer = new ZdfVideoUrlOptimizer();

  public PhoenixFilmDetailTask(AbstractCrawler aCrawler,
      ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, Optional<String> aAuthKey,
      String filmDetailHost, String videoDetailHost) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    this.filmDetailHost = filmDetailHost;
    this.videoDetailHost = videoDetailHost;

    registerJsonDeserializer(OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new PhoenixFilmDetailDeserializer());
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }


  @Override
  protected AbstractRecrusivConverterTask<Film, CrawlerUrlDTO> createNewOwnInstance(
      ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixFilmDetailTask(this.crawler, aElementsToProcess, this.authKey, this.filmDetailHost, this.videoDetailHost);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    Optional<PhoenixFilmDetailDto> filmDetailDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
    if (!filmDetailDtoOptional.isPresent()) {
      // tritt auf, wenn kein Film vorhanden
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    PhoenixFilmDetailDto filmDetailDto = filmDetailDtoOptional.get();
    Optional<PhoenixFilmXmlHandler> filmXmlDtoOptional = loadFilmXml(filmDetailDto.getBaseName());
    if (!filmXmlDtoOptional.isPresent()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    PhoenixFilmXmlHandler filmXmlHandler = filmXmlDtoOptional.get();
    if (filmXmlHandler.getBaseName() == null) {
      // tritt auf, wenn kein Film vorhanden
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    Optional<DownloadDto> videoDetailDtoOptional = deserializeOptional(createWebTarget(videoDetailHost + PhoenixConstants.URL_VIDEO_DETAILS_BASE + filmXmlHandler.getBaseName()),
        OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);
    if (!videoDetailDtoOptional.isPresent()) {
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
      return;
    }

    try {
      addFilm(filmDetailDto, filmXmlHandler, videoDetailDtoOptional.get());
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    } catch (MalformedURLException e) {
      LOG.error("PhoenixFilmDetailTask: url can't be parsed: ", e);
      crawler.incrementAndGetErrorCount();
      crawler.updateProgress();
    }
  }

  private void addFilm(PhoenixFilmDetailDto filmDetailDto, PhoenixFilmXmlHandler filmXmlHandler, DownloadDto downloadDto)
      throws MalformedURLException {
    Film film = new Film(UUID.randomUUID(), Sender.PHOENIX, filmDetailDto.getTitle(), filmDetailDto.getTopic(), filmXmlHandler.getTime(),
        filmXmlHandler.getDuration());

    film.setBeschreibung(filmDetailDto.getDescription());

    if (filmDetailDto.getWebsite().isPresent()) {
      try {
        film.setWebsite(new URL(filmDetailDto.getWebsite().get()));
      } catch (MalformedURLException ex) {
        LOG.error(String.format("A website URL \"%s\" isn't valid.", filmDetailDto.getWebsite()), ex);
      }
    }

    DownloadDtoFilmConverter.addUrlsToFilm(film, downloadDto, Optional.of(optimizer));
    taskResults.add(film);
  }

  private Optional<PhoenixFilmXmlHandler> loadFilmXml(String baseName) {
    final String xmlUrl = this.filmDetailHost + PhoenixConstants.URL_FILM_DETAIL_XML + baseName;

    try {

      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      PhoenixFilmXmlHandler handler = new PhoenixFilmXmlHandler();
      saxParser.parse(xmlUrl, handler);

      return Optional.of(handler);

    } catch (SAXException | IOException | ParserConfigurationException e) {
      LOG.error(String.format("Error loading xml document \"%s\".", xmlUrl), e);
    }

    return Optional.empty();
  }
}
