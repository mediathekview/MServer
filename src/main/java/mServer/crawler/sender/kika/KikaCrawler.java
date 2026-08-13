package mServer.crawler.sender.kika;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.kika.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KikaCrawler extends MediathekCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

  public KikaCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.KIKA, 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    int maxPages = 0;

    if (CrawlerTool.loadShort()) {
      maxPages = 1;
    } else if (CrawlerTool.loadLong()) {
      maxPages = 10;
    } else if (CrawlerTool.loadMax()) {
      maxPages = 999;
    } else if (CrawlerTool.loadLongMax()) {
      maxPages = 999;
    }

    try {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> root = new ConcurrentLinkedQueue<>();
      for (char c = 'A'; c <= 'Z'; c++) {
        root.add(new CrawlerUrlDTO(String.format(KikaConstants.URL_LETTER_PAGE, c)));
      }
      root.add(new CrawlerUrlDTO(String.format(KikaConstants.URL_LETTER_PAGE, "...")));

      // we get channel and broadcastSeries (brand) from letters
      final KikaLetterPageTask aKikaLetterPageTask = new KikaLetterPageTask(this, root, maxPages);
      final Queue<KikaEntityDto> brandsAndChannel = new ConcurrentLinkedQueue<>();
      brandsAndChannel.addAll(aKikaLetterPageTask.fork().join());
      Log.sysLog("KIKA brandsAndChannel: " + brandsAndChannel.size());
      // lets take the channels and resolve them to all kinds
      ConcurrentLinkedQueue<KikaEntityDto> channelQueue = brandsAndChannel.stream()
          .filter(v -> v.getDocType().filter("channel"::equals).isPresent())
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      final KikaChannelPageTask aKikaChannelPageTask = new KikaChannelPageTask(this, channelQueue, maxPages);
      final Queue<KikaEntityDto> mixedKindsFromChannel = new ConcurrentLinkedQueue<>();
      mixedKindsFromChannel.addAll(aKikaChannelPageTask.fork().join());
      Log.sysLog("KIKA mixedKindsFromChannel: " + mixedKindsFromChannel.size());
      // lets use brands from letters and channels to resolve videoSubchannel
      ConcurrentLinkedQueue<KikaEntityDto> brandQueue = Stream.concat(brandsAndChannel.stream(), mixedKindsFromChannel.stream())
          .filter(v -> v.getDocType().filter("broadcastSeries"::equals).isPresent())
          .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
      // add videoSubchannel and subchannel (same structure) from channels and add them to the result from brands
      final ConcurrentLinkedQueue<KikaEntityDto> videoSubchannel = mixedKindsFromChannel.stream()
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
      Log.sysLog("KIKA brandQueue: " + brandQueue.size());
      // resolve brands from letters and channels to videoSubchannel
      final KikaBrandPageTask aKikaBrandPageTask = new KikaBrandPageTask(this, brandQueue);
      videoSubchannel.addAll(aKikaBrandPageTask.fork().join());
      Log.sysLog("KIKA videoSubchannel: " + videoSubchannel.size());
      // a videoSubchannel to video
      final ConcurrentLinkedQueue<KikaFilmDto> videoAsset = new ConcurrentLinkedQueue<>();
      final KikaVideoSubchannelPageTask aKikaVideoSubchannelPageTask = new KikaVideoSubchannelPageTask(this, videoSubchannel, maxPages);
      videoAsset.addAll(aKikaVideoSubchannelPageTask.fork().join());
      // add assets to the video
      final ConcurrentLinkedQueue<KikaFilmDto> videoWithAssets = new ConcurrentLinkedQueue<>();
      final KikaAssetsPageTask aKikaAssetsPageTask = new KikaAssetsPageTask(this, videoAsset);
      videoWithAssets.addAll(aKikaAssetsPageTask.fork().join());
      //
      Log.sysLog("KIKA: Anzahl: " + videoWithAssets.size());
      meldungAddMax(videoWithAssets.size());

      // convert video with assets to a film
      return new KikaConverterTask(this, videoWithAssets);
    } catch (final Exception ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    return null;
  }

}
