package mServer.crawler.sender.phoenix.tasks;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.DatenFilm;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.WebTarget;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.orf.CrawlerUrlDTO;
import mServer.crawler.sender.orf.tasks.AbstractRecursivConverterTask;
import mServer.crawler.sender.phoenix.DownloadDto;
import mServer.crawler.sender.phoenix.PhoenixConstants;
import mServer.crawler.sender.phoenix.parser.PhoenixFilmDetailDeserializer;
import mServer.crawler.sender.phoenix.parser.PhoenixFilmDetailDto;
import mServer.crawler.sender.phoenix.parser.PhoenixFilmXmlHandler;
import mServer.crawler.sender.phoenix.parser.ZdfDownloadDtoDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

public class PhoenixFilmDetailTask extends ZdfTaskBase<DatenFilm, CrawlerUrlDTO> {

  private static final Logger LOG = LogManager.getLogger(PhoenixFilmDetailTask.class);

  private static final Type OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN = new TypeToken<Optional<PhoenixFilmDetailDto>>() {
  }.getType();
  private static final Type OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN = new TypeToken<Optional<DownloadDto>>() {
  }.getType();

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  private final String filmDetailHost;
  private final String videoDetailHost;

  public PhoenixFilmDetailTask(MediathekReader aCrawler,
          ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, Optional<String> aAuthKey,
          String filmDetailHost, String videoDetailHost) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    this.filmDetailHost = filmDetailHost;
    this.videoDetailHost = videoDetailHost;

    registerJsonDeserializer(OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN, new PhoenixFilmDetailDeserializer());
    registerJsonDeserializer(OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN, new ZdfDownloadDtoDeserializer());
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, CrawlerUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<CrawlerUrlDTO> aElementsToProcess) {
    return new PhoenixFilmDetailTask(this.crawler, aElementsToProcess, this.authKey, this.filmDetailHost, this.videoDetailHost);
  }

  @Override
  protected void processRestTarget(CrawlerUrlDTO aDTO, WebTarget aTarget) {
    try {
      Optional<PhoenixFilmDetailDto> filmDetailDtoOptional = deserializeOptional(aTarget, OPTIONAL_FILM_DETAIL_DTO_TYPE_TOKEN);
      if (!filmDetailDtoOptional.isPresent()) {
        // tritt auf, wenn kein Film vorhanden
        return;
      }

      PhoenixFilmDetailDto filmDetailDto = filmDetailDtoOptional.get();
      Optional<PhoenixFilmXmlHandler> filmXmlDtoOptional = loadFilmXml(filmDetailDto.getBaseName());
      if (!filmXmlDtoOptional.isPresent()) {
        LOG.info("PhoenixFilmDetailTask: error parsing xml " + aDTO.getUrl());
        return;
      }

      PhoenixFilmXmlHandler filmXmlHandler = filmXmlDtoOptional.get();
      if (filmXmlHandler.getBaseName() == null) {
        // tritt auf, wenn kein Film vorhanden
        return;
      }

      Optional<DownloadDto> videoDetailDtoOptional = deserializeOptional(createWebTarget(videoDetailHost + PhoenixConstants.URL_VIDEO_DETAILS_BASE + filmXmlHandler.getBaseName()),
              OPTIONAL_DOWNLOAD_DTO_TYPE_TOKEN);
      if (!videoDetailDtoOptional.isPresent()) {
        LOG.info("PhoenixFilmDetailTask: error deserializing download dto " + aDTO.getUrl());
        return;
      }

      addFilm(filmDetailDto, filmXmlHandler, videoDetailDtoOptional.get());

    } catch (MalformedURLException e) {
      LOG.error("PhoenixFilmDetailTask: url can't be parsed: ", e);
    } catch (Exception e) {
      // catch all exceptions to ensure that the crawler can process the other results
      LOG.fatal(e);
    }
  }

  private void addFilm(PhoenixFilmDetailDto filmDetailDto, PhoenixFilmXmlHandler filmXmlHandler, DownloadDto downloadDto)
          throws MalformedURLException {

    String datum = filmXmlHandler.getTime().format(DATE_FORMAT);
    String zeit = filmXmlHandler.getTime().format(TIME_FORMAT);

    final DatenFilm film = new DatenFilm(crawler.getSendername(),
            filmDetailDto.getTopic(),
            filmDetailDto.getWebsite().get(),
            filmDetailDto.getTitle(),
            downloadDto.getUrl(Qualities.NORMAL).get(),
            "",
            datum,
            zeit,
            filmXmlHandler.getDuration().getSeconds(),
            filmDetailDto.getDescription());

    if (downloadDto.getUrl(Qualities.HD).isPresent()) {
      CrawlerTool.addUrlHd(film, downloadDto.getUrl(Qualities.HD).get(), "");
    }
    if (downloadDto.getUrl(Qualities.SMALL).isPresent()) {
      CrawlerTool.addUrlKlein(film, downloadDto.getUrl(Qualities.SMALL).get(), "");
    }
    if (downloadDto.getSubTitleUrl().isPresent()) {
      CrawlerTool.addUrlSubtitle(film, downloadDto.getSubTitleUrl().get());
    }
    if (downloadDto.getGeoLocation().isPresent()) {
      film.arr[DatenFilm.FILM_GEO] = downloadDto.getGeoLocation().get().getDescription();
    }
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
