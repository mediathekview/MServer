package mServer.crawler.sender.livestream;

import de.mediathekview.mlib.daten.DatenFilm;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.livestream.tasks.LivestreamArdOverviewTask;
import mServer.crawler.sender.livestream.tasks.LivestreamArdStreamTask;
import mServer.crawler.sender.livestream.tasks.LivestreamToFilmTask;
import mServer.crawler.sender.orf.TopicUrlDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

public class LivestreamCrawler  extends MediathekCrawler {
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public LivestreamCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, "Livestream", 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    //
    // ARD
    //
    final Set<TopicUrlDTO> ardLivestreamsLinks;
    final LivestreamArdOverviewTask ard = new LivestreamArdOverviewTask(
            this,
            new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_ARD_LIVESTREAMS) } ) ) );
    ardLivestreamsLinks = forkJoinPool.invoke(ard);
    //
    final Set<TopicUrlDTO> ardLivestreams;
    final LivestreamArdStreamTask lardS = new LivestreamArdStreamTask(this, new ConcurrentLinkedQueue<>(ardLivestreamsLinks));
    ardLivestreams = forkJoinPool.invoke(lardS);
    //ardLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // ZDF
    //
/*    final LocalDateTime now = LocalDateTime.now();
    final String dt = now.format(DAY_PAGE_DATE_FORMATTER);
    final Set<TopicUrlDTO> zdfLivestreams;
    final String zdfQueryUrl = String.format(LivestreamConstants.URL_ZDF_LIVESTREAMS, dt);
    final LivestreamZdfOverviewTask zdf = new LivestreamZdfOverviewTask(
            this,
            new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(zdfQueryUrl) } ) ) );
    zdfLivestreams = forkJoinPool.invoke(zdf);
    //zdfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // ORF
    //
    final Set<TopicUrlDTO> orfLivestreamsLinks;
    final LivestreamOrfOverviewTask orf = new LivestreamOrfOverviewTask(
            this,
            new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_ORF_LIVESTREAMS) } ) ) );
    orfLivestreamsLinks = forkJoinPool.invoke(orf);
    //
    final Set<TopicUrlDTO> orfLivestreams;
    final LivestreamOrfStreamTask lorfs = new LivestreamOrfStreamTask(this, new ConcurrentLinkedQueue<>(orfLivestreamsLinks));
    orfLivestreams = forkJoinPool.invoke(lorfs);
    //orfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
    // SRF
    //
    final Set<TopicUrlDTO> srfLivestreamsLinks;
    final LivestreamSrfOverviewTask srf = new LivestreamSrfOverviewTask(
            this,
            new ConcurrentLinkedQueue<>( Arrays.asList( new CrawlerUrlDTO[] { new CrawlerUrlDTO(LivestreamConstants.URL_SRF_LIVESTREAMS) } ) ) );
    srfLivestreamsLinks = forkJoinPool.invoke(srf);
    //
    final Set<TopicUrlDTO> srfLivestreams;
    final LivestreamSrfStreamTask lsrfs = new LivestreamSrfStreamTask(this, new ConcurrentLinkedQueue<>(srfLivestreamsLinks));
    srfLivestreams = forkJoinPool.invoke(lsrfs);
    //srfLivestreams.forEach(a-> { System.out.println(a.getTopic() + " " + a.getUrl());});
    //
  */
    HashSet<TopicUrlDTO> allLivestreams = new HashSet<TopicUrlDTO>();
    allLivestreams.addAll(ardLivestreams);
    /*allLivestreams.addAll(zdfLivestreams);
    allLivestreams.addAll(orfLivestreams);
    allLivestreams.addAll(srfLivestreams);
*/
    return new LivestreamToFilmTask(this, new ConcurrentLinkedQueue<>(allLivestreams));
  }
}
