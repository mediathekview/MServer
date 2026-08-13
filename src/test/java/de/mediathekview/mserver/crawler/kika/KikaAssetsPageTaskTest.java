package de.mediathekview.mserver.crawler.kika;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.kika.tasks.KikaAssetsPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaAssetsPageTaskTest extends WireMockTestBase {
  
  @Test
  void testAssetExtraction() {
    setupSuccessfulJsonResponse("/KikaAsset", "/kika/KikaAsset.json");
    Set<KikaFilmDto> result = executeTask("/KikaAsset");
    List<KikaAssetDto> expectedResult = generateExpectedResult();
    assertEquals(1 ,result.size(), "Must be one Result since we gave one film");
    List<KikaAssetDto> resultAssets = result.stream().findAny().get().getAssets();
    assertEquals(expectedResult.size(), resultAssets.size(), "Missing video resource in film");
    assertIterableEquals(expectedResult, resultAssets, "All properies of an Asset must be matching");
  }
  
  private Set<KikaFilmDto> executeTask(String... requestUrl) {
    final Queue<KikaFilmDto> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new KikaFilmDto(
          Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
          Optional.empty(), Optional.empty(), Optional.of(getWireMockBaseUrlSafe() + url), Optional.empty(),
          Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
          Optional.empty(), Optional.empty(), Optional.empty(),
          Optional.empty(), Optional.empty()));
    }
    return new KikaAssetsPageTask(createCrawler(), input).invoke();
  }
  
  private List<KikaAssetDto> generateExpectedResult() {
    return new ArrayList<>(asList(
        new KikaAssetDto(
            Optional.of("640x360"),                     // profileName
            Optional.of("TV-20161208-1444-1900.ln.mp4"), // fileName
            Optional.of(0),                              // fileSize
            Optional.empty(),                            // mediaType
            Optional.of(640),                             // frameWidth
            Optional.of(360),                             // frameHeight
            Optional.of(-1),                              // bitrateVideo
            Optional.of(-1),                              // bitrateAudio
            Optional.of("progressive"),                   // type
            Optional.of("https://ndr-progressive.ard-mcdn.de/progressive/2016/1208/TV-20161208-1444-1900.ln.mp4"), // url
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/subtitle"), // videoSubtitle
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/webvtt") // webvttUrl
        ),    
        new KikaAssetDto(
            Optional.of("960x540"),
            Optional.of("TV-20161208-1444-1900.hq.mp4"),
            Optional.of(0),
            Optional.empty(),
            Optional.of(960),
            Optional.of(540),
            Optional.of(-1),
            Optional.of(-1),
            Optional.of("progressive"),
            Optional.of("https://ndr-progressive.ard-mcdn.de/progressive/2016/1208/TV-20161208-1444-1900.hq.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/webvtt")
        ),
        new KikaAssetDto(
            Optional.of("Video 2018 | MP4 720p25 | Web XL| 16:9 | 1280x720"),
            Optional.of("TV-20161208-1444-1900.hd.mp4"),
            Optional.of(0),
            Optional.empty(),
            Optional.of(1280),
            Optional.of(720),
            Optional.of(-1),
            Optional.of(-1),
            Optional.of("progressive"),
            Optional.of("https://ndr-progressive.ard-mcdn.de/progressive/2016/1208/TV-20161208-1444-1900.hd.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/webvtt")
        ),
        new KikaAssetDto(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of("auto"),
            Optional.of("https://adaptive.ndr.de/i/ndr/2016/1208/TV-20161208-1444-1900.,ln,hd,hq,mn,.mp4.csmil/master.m3u8"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/giftige-absichten-104/webvtt")
        )
    ));
  }
  
  protected KikaCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    return new KikaCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
