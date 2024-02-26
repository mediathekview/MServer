package de.mediathekview.mserver.crawler.orfon.task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.UUID;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

public class OrfOnVideoInfo2FilmTask extends AbstractRecursiveConverterTask<Film, OrfOnVideoInfoDTO> {
  private static final long serialVersionUID = 1L;
  private static final String ORF_AUDIODESCRIPTION_PREFIX = "AD | ";
  
  public OrfOnVideoInfo2FilmTask(AbstractCrawler aCrawler, Queue<OrfOnVideoInfoDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }  
  
  @Override
  protected AbstractRecursiveConverterTask<Film, OrfOnVideoInfoDTO> createNewOwnInstance(
      Queue<OrfOnVideoInfoDTO> aElementsToProcess) {
    return new OrfOnVideoInfo2FilmTask(this.crawler, aElementsToProcess);
  }
  @Override
  protected Integer getMaxElementsToProcess() {
    return crawler.getCrawlerConfig().getMaximumUrlsPerTask();
  }
  @Override
  protected void processElement(OrfOnVideoInfoDTO aElement) {
    Film aFilm = new Film(
        UUID.randomUUID(),
        Sender.ORF,
        buildTitle(aElement.getTitle().get(), aElement.getTopic().get()),
        buildTopic(aElement.getTitle().get(), aElement.getTopic().get(), aElement.getTopicForArchive().orElse("")),
        aElement.getAired().orElse(LocalDateTime.of(1970,1,1,00,00,00)),
        aElement.getDuration().orElse(Duration.ofMinutes(0L))
        );
    aElement.getGeorestriction().ifPresent(aFilm::addAllGeoLocations);
    aElement.getDescription().ifPresent(aFilm::setBeschreibung);
    aElement.getVideoUrls().ifPresent(aFilm::setUrls);
    aElement.getWebsite().ifPresent(aFilm::setWebsite);
    aElement.getSubtitleUrls().ifPresent(aFilm::addAllSubtitleUrls);
    crawler.incrementAndGetActualCount();
    taskResults.add(aFilm);
    
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
