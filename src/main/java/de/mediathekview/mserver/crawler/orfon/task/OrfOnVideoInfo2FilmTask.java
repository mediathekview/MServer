package de.mediathekview.mserver.crawler.orfon.task;

import java.util.Queue;
import java.util.UUID;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

public class OrfOnVideoInfo2FilmTask extends AbstractRecursiveConverterTask<Film, OrfOnVideoInfoDTO> {
  private static final long serialVersionUID = 1L;
  
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
        aElement.getAired().get(),
        aElement.getDuration().get()
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
    if (newTopic.startsWith("AD | ")) {
      newTopic = newTopic.replace("AD | ", "");
    }
    if (newTopic.equalsIgnoreCase("archiv")) {
      newTopic = archiveTopic.replace("History | ", "");
    }
    return newTopic;
  }
  
  private String buildTitle(String title, String topic) {
    if (title.startsWith("AD | ")) {
      return title.replace("AD | ", "").concat(" (Audiodeskription)");
    }
    return title;
  }

}
