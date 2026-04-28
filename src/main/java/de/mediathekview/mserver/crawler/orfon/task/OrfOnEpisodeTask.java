package de.mediathekview.mserver.crawler.orfon.task;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractJsonRestTask;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;
import de.mediathekview.mserver.crawler.orfon.json.OrfOnEpisodeDeserializer;
import jakarta.ws.rs.core.Response;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnEpisodeTask extends AbstractJsonRestTask<Film, OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 3272445100769901305L;
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeTask.class);
  private static final String ORF_AUDIODESCRIPTION_PREFIX = "AD | ";

  public OrfOnEpisodeTask(AbstractCrawler crawler, Queue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, OrfOnConstants.AUTH);
  }

  @Override
  protected JsonDeserializer<OrfOnVideoInfoDTO> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnEpisodeDeserializer(this.crawler);
  }

  @Override
  protected Type getType() {
    return new TypeToken<OrfOnVideoInfoDTO>() {}.getType();
  }

  @Override
  protected void postProcessing(OrfOnVideoInfoDTO aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    if (aResponseObj.getTitle().isEmpty() && aResponseObj.getTitleWithDate().isEmpty()) {
      LOG.warn("Missing title for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getTopic().isEmpty()) {
      LOG.warn("Missing topic for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getVideoUrls().isEmpty()) {
      LOG.warn("Missing videoUrls for {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getDrmProtected().orElse("false").equalsIgnoreCase("true")) {
      LOG.warn("Ignore DRM Protected {}", aDTO);
      crawler.incrementAndGetErrorCount();
      return;
    }
    if (aResponseObj.getDuration().isEmpty()) {
      LOG.warn("Missing duration for {}", aDTO);
    }
    if (aResponseObj.getAired().isEmpty()) {
      LOG.warn("Missing aired date for {}", aDTO);
    }
    if (aResponseObj.getWebsite().isEmpty()) {
      LOG.warn("Missing website for {}", aDTO);
    }
    Film aFilm = new Film(
        UUID.randomUUID(),
        Sender.ORF,
        buildTitle(aResponseObj.getTitle().get(), aResponseObj.getTopic().get()),
        buildTopic(aResponseObj.getTitle().get(), aResponseObj.getTopic().get(), aResponseObj.getTopicForArchive().orElse("")),
        aResponseObj.getAired().orElse(LocalDateTime.of(1970,1,1,00,00,00)),
        aResponseObj.getDuration().orElse(Duration.ofMinutes(0L))
        );
    aResponseObj.getId().ifPresent(aFilm::setId);
    aResponseObj.getGeorestriction().ifPresent(aFilm::addAllGeoLocations);
    aResponseObj.getDescription().ifPresent(aFilm::setBeschreibung);
    aResponseObj.getVideoUrls().ifPresent(aFilm::setUrls);
    aResponseObj.getWebsite().ifPresent(aFilm::setWebsite);
    aResponseObj.getSubtitleUrls().ifPresent(aFilm::addAllSubtitleUrls);
    crawler.incrementAndGetActualCount();
    taskResults.add(aFilm);
  }

  @Override
  protected AbstractRecursiveConverterTask<Film, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(
      Queue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnEpisodeTask(crawler, aElementsToProcess);
  } 

  @Override
  protected void handleHttpError(OrfOnBreadCrumsUrlDTO dto, URI url, Response response) {
      crawler.printErrorMessage();
      LOG.fatal(
          "A HTTP error {} occurred when getting REST information from: \"{}\".",
          response.getStatus(),
          url);
  }

  private String buildTopic(String title, String topic, String archiveTopic) {
    String newTopic = topic;
    if (newTopic.startsWith(ORF_AUDIODESCRIPTION_PREFIX)) {
      newTopic = newTopic.replace(ORF_AUDIODESCRIPTION_PREFIX, "");
    }
    if (newTopic.equalsIgnoreCase("archiv")) {
      newTopic = archiveTopic.replace("History | ", "");
    }
    return newTopic;
  }
  
  private String buildTitle(String title, String topic) {
    if (title.startsWith(ORF_AUDIODESCRIPTION_PREFIX)) {
      return title.replace(ORF_AUDIODESCRIPTION_PREFIX, "").concat(" (Audiodeskription)");
    }
    return title;
  }
}
