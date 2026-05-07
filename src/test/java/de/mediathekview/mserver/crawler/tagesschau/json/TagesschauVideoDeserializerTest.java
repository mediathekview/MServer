package de.mediathekview.mserver.crawler.tagesschau.json;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauCrawler;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.junit.jupiter.api.Test;

class TagesschauVideoDeserializerTest {
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  @Test
  void test() {

    final JsonElement jsonElement =
        JsonFileReader.readJson("/tagesschau/tagesschau_20jahre_video.json");
    TagesschauVideoDeserializer target = new TagesschauVideoDeserializer(createCrawler());
    final List<Film> actual = target.deserialize(jsonElement, null, null);
    assertNotNull(actual);
    assertEquals(1, actual.size());
    AssertFilm.assertEquals(
        actual.getFirst(),
        Sender.TAGESSCHAU24,
        "tagesschau vor 20 Jahren",
        "tagesschau vor 20 Jahren, 30. Januar 2006",
        LocalDateTime.of(2006, 1, 30, 20, 0, 0),
        Duration.ofSeconds(937),
        "",
        "https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547686.html",
        new GeoLocations[] {GeoLocations.GEO_NONE},
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webm.h264.mp4",
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webxl.h264.mp4",
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webxxl.h264.mp4",
        "");
  }

  protected TagesschauCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new TagesschauCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
