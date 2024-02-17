package de.mediathekview.mserver.crawler.orfon.task;

import java.util.Queue;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecursiveConverterTask;
import de.mediathekview.mserver.crawler.orfon.OrfOnVideoInfoDTO;

public class OrfOnVideoInfo2FilmTask extends AbstractRecursiveConverterTask<Film, OrfOnVideoInfoDTO> {
  private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(OrfOnVideoInfo2FilmTask.class);
  
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
        buildTopic(aElement.getTitle().get(), aElement.getTopic().get()),
        aElement.getAired().get(),
        aElement.getDuration().get()
        );
    aElement.getGeorestriction().ifPresent(aFilm::addAllGeoLocations);
    aElement.getDescription().ifPresent(aFilm::setBeschreibung);
    aElement.getVideoUrls().ifPresent(aFilm::setUrls);
    aElement.getWebsite().ifPresent(aFilm::setWebsite);
    
    taskResults.add(aFilm);
    
  }
  
  private String buildTopic(String title, String topic) {
    if (topic.startsWith("AD | ")) {
      return topic.replace("AD | ", "");
    }
    return topic;
  }
  
  private String buildTitle(String title, String topic) {
    if (title.startsWith("AD | ")) {
      return title.replace("AD | ", "").concat(" (Audiodeskription)");
    }
    return title;
  }

  









}
