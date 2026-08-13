package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.tasks.KikaAssetsPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaBrandPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaChannelPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaConverterTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaVideoSubchannelPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

  public KikaCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    int max_pages = crawlerConfig.getMaximumSubpages().intValue();
    try {
      final Queue<CrawlerUrlDTO> root = new ConcurrentLinkedQueue<>();
      for (char c = 'A'; c <= 'Z'; c++) {
        root.add(new CrawlerUrlDTO(String.format(KikaConstants.URL_LETTER_PAGE, c)));
      }
      root.add(new CrawlerUrlDTO(String.format(KikaConstants.URL_LETTER_PAGE, "...")));
      
      //
      //root.clear();
      //root.add(new CrawlerUrlDTO(String.format(KikaConstants.URL_LETTER_PAGE, "A")));
      //
      // we get channel and broadcastSeries (brand) from letters
      final KikaLetterPageTask aKikaLetterPageTask = new KikaLetterPageTask(this, root, max_pages);
      final Queue<KikaEntityDto> brandsAndChannel = new ConcurrentLinkedQueue<>();
      brandsAndChannel.addAll(aKikaLetterPageTask.fork().join());
      // lets take the channels and resolve them to all kinds
      Queue<KikaEntityDto> channelQueue = brandsAndChannel.stream()
          .filter(v -> v.getDocType().filter("channel"::equals).isPresent())
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      final KikaChannelPageTask aKikaChannelPageTask = new KikaChannelPageTask(this, channelQueue, max_pages);
      final Queue<KikaEntityDto> mixedKindsFromChannel = new ConcurrentLinkedQueue<>();
      mixedKindsFromChannel.addAll(aKikaChannelPageTask.fork().join());
      // lets use brands from letters and channels to resolve videoSubchannel
      Queue<KikaEntityDto> brandQueue = Stream.concat(brandsAndChannel.stream(), mixedKindsFromChannel.stream())
          .filter(v -> v.getDocType().filter("broadcastSeries"::equals).isPresent())
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      // add videoSubchannel and subchannel (same structure) from channels and add them to the result from brands
      final Queue<KikaEntityDto> videoSubchannel = mixedKindsFromChannel.stream()
          .filter(v -> v.getDocType().filter(t -> t.equals("videoSubchannel") || t.equals("subchannel")).isPresent())
          .map(v -> {
            final String suffix = v.getDocType().filter("subchannel"::equals).isPresent()
                ? "/teasers?page=0"
                : "/videos?page=0&videoType=mainContent";
            return new KikaEntityDto(
                v.getDocType(), v.getId(), v.getUuid(), v.getExternalId(),
                v.getUrlPath(), v.getApiId(),
                Optional.of(v.getUrl().replace("/relatedvideos/", "/videos/") + suffix),
                v.getModificationDate());
          })
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      // resolve brands from letters and channels to videoSubchannel
      final KikaBrandPageTask aKikaBrandPageTask = new KikaBrandPageTask(this, brandQueue);
      videoSubchannel.addAll(aKikaBrandPageTask.fork().join());
      // a videoSubchannel to video
      final Queue<KikaFilmDto> videoAsset = new ConcurrentLinkedQueue<>();
      final KikaVideoSubchannelPageTask aKikaVideoSubchannelPageTask = new KikaVideoSubchannelPageTask(this, videoSubchannel, max_pages);
      videoAsset.addAll(aKikaVideoSubchannelPageTask.fork().join());
      // add assets to the video
      final Queue<KikaFilmDto> videoWithAssets = new ConcurrentLinkedQueue<>();
      final KikaAssetsPageTask aKikaAssetsPageTask = new KikaAssetsPageTask(this, videoAsset);
      videoWithAssets.addAll(aKikaAssetsPageTask.fork().join());
      // filter logic for database
      final Queue<KikaFilmDto> videosFiltered = this.filterExistingFilms(videoWithAssets, v -> v.getId().get() );
      //
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), videosFiltered.size());
      getAndSetMaxCount(videosFiltered.size());
      // convert video with assets to a film
      return new KikaConverterTask(this, videosFiltered);
    } catch (final Exception ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    return null;
  }

}
