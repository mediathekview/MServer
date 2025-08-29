package mServer.crawler.sender.orfon.task;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.core.Response;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractJsonRestTask;
import mServer.crawler.sender.base.AbstractRecursivConverterTask;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.orfon.OrfOnBreadCrumsUrlDTO;
import mServer.crawler.sender.orfon.OrfOnConstants;
import mServer.crawler.sender.orfon.OrfOnVideoInfoDTO;
import mServer.crawler.sender.orfon.json.OrfOnEpisodeDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

// <T, R, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D>
// return T Class from this task, desirialisation of class R , D , Reasearch in this url
public class OrfOnEpisodeTask extends AbstractJsonRestTask<DatenFilm, OrfOnVideoInfoDTO, OrfOnBreadCrumsUrlDTO> {
  private static final long serialVersionUID = 3272445100769901305L;
  private static final Logger LOG = LogManager.getLogger(OrfOnEpisodeTask.class);
  private static final String ORF_AUDIODESCRIPTION_PREFIX = "AD | ";

  private static final DateTimeFormatter DATE_FORMAT
          = DateTimeFormatter.ofPattern("dd.MM.yyyy");
  private static final DateTimeFormatter TIME_FORMAT
          = DateTimeFormatter.ofPattern("HH:mm:ss");

  public OrfOnEpisodeTask(MediathekReader crawler, ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> urlToCrawlDTOs) {
    super(crawler, urlToCrawlDTOs, Optional.of(OrfOnConstants.AUTH));
  }

  @Override
  protected JsonDeserializer<OrfOnVideoInfoDTO> getParser(OrfOnBreadCrumsUrlDTO aDTO) {
    return new OrfOnEpisodeDeserializer();
  }

  @Override
  protected Type getType() {
    return new TypeToken<OrfOnVideoInfoDTO>() {
    }.getType();
  }

  @Override
  protected void postProcessing(OrfOnVideoInfoDTO aResponseObj, OrfOnBreadCrumsUrlDTO aDTO) {
    if (aResponseObj.getTitle().isEmpty() && aResponseObj.getTitleWithDate().isEmpty()) {
      LOG.warn("Missing title for {}", aDTO);
      return;
    }
    if (aResponseObj.getTopic().isEmpty()) {
      LOG.warn("Missing topic for {}", aDTO);
      return;
    }
    if (aResponseObj.getVideoUrls().isEmpty()) {
      LOG.warn("Missing videoUrls for {}", aDTO);
      return;
    }
    if (aResponseObj.getDrmProtected().orElse("false").equalsIgnoreCase("true")) {
      LOG.warn("Ignore DRM Protected {}", aDTO);
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

    final LocalDateTime dateTime = aResponseObj.getAired().orElse(LocalDateTime.of(1970, 1, 1, 0, 0, 0));
    String dateValue = dateTime.format(DATE_FORMAT);
    String timeValue = dateTime.format(TIME_FORMAT);

    final Optional<Map<Qualities, String>> videoUrls = aResponseObj.getVideoUrls();
    if (videoUrls.isPresent() && !videoUrls.get().isEmpty()) {
      final Map<Qualities, String> urls = videoUrls.get();
      DatenFilm film = new DatenFilm(Const.ORF,
              buildTopic(aResponseObj.getTopic().orElse(""), aResponseObj.getTopicForArchive().orElse("")),
              aResponseObj.getWebsite().orElse(""),
              buildTitle(aResponseObj.getTitle().orElse("")),
              urls.getOrDefault(Qualities.NORMAL, ""), "",
              dateValue, timeValue,
              aResponseObj.getDuration().orElse(Duration.ofMinutes(0L)).getSeconds(),
              aResponseObj.getDescription().orElse(""));
      if (urls.containsKey(Qualities.SMALL)) {
        CrawlerTool.addUrlKlein(film, urls.get(Qualities.SMALL));
      }
      if (urls.containsKey(Qualities.HD)) {
        CrawlerTool.addUrlHd(film, urls.get(Qualities.HD));
      }

      final Optional<String> subtitleUrl = aResponseObj.getSubtitleUrl();
      if (subtitleUrl.isPresent()) {
        CrawlerTool.addUrlSubtitle(film, subtitleUrl.get());
      }
      taskResults.add(film);
    }
  }

  @Override
  protected AbstractRecursivConverterTask<DatenFilm, OrfOnBreadCrumsUrlDTO> createNewOwnInstance(
          ConcurrentLinkedQueue<OrfOnBreadCrumsUrlDTO> aElementsToProcess) {
    return new OrfOnEpisodeTask(crawler, aElementsToProcess);
  }

  @Override
  protected void handleHttpError(OrfOnBreadCrumsUrlDTO dto, URI url, Response response) {
    Log.errorLog(87732933, "ORF: http error " + response.getStatus() + ", " + url);
    LOG.fatal(
            "A HTTP error {} occurred when getting REST information from: \"{}\".",
            response.getStatus(),
            url);
  }

  private String buildTopic(String topic, String archiveTopic) {
    String newTopic = topic;
    if (newTopic.startsWith(ORF_AUDIODESCRIPTION_PREFIX)) {
      newTopic = newTopic.replace(ORF_AUDIODESCRIPTION_PREFIX, "");
    }
    if (newTopic.equalsIgnoreCase("archiv")) {
      newTopic = archiveTopic.replace("History | ", "");
    }
    return newTopic;
  }

  private String buildTitle(String title) {
    if (title.startsWith(ORF_AUDIODESCRIPTION_PREFIX)) {
      return title.replace(ORF_AUDIODESCRIPTION_PREFIX, "").concat(" (Audiodeskription)");
    }
    return title;
  }
}
