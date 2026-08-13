package de.mediathekview.mserver.crawler.kika;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.kika.tasks.KikaConverterTask;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaConverterTaskTest {
  
  @Test
  void testConvertKikaFilmDtoToFilm() {
    final Queue<KikaFilmDto> videosFiltered = new ConcurrentLinkedQueue<>();
    videosFiltered.add(getInput());
    Collection<Film> result = new KikaConverterTask(createCrawler(), videosFiltered).fork().join();

    assertEquals(1, result.size(), "Es muss genau ein Film erzeugt werden");
    Film actual = result.stream().findFirst().orElseThrow();

    assertEquals(Sender.KIKA, actual.getSender(), "Sender muss KiKA sein");
    assertEquals("Dein Song - Zurück im Wettbewerb", actual.getThema(), "Thema muss übernommen werden");
    assertEquals("4. Wie wird ein Song zum Hit?", actual.getTitel(), "Titel muss übernommen werden");
    assertEquals("Auf der Suche nach dem WOW-Moment arbeiten Lisa und Lukas am Refrain von \"Imperfections\" und zeigen Jules am goldenen Mikrofon, welche Power in ihren Stimmen steckt. Paula wagt ein Experiment: Mit ihrem Waldhorn will sie ihrem Song \"Escape\" noch einen ganz besonderen Sound geben. Und Sängerin LOTTE hat für die drei einen unschlagbaren Tipp.", actual.getBeschreibung(), "Beschreibung");
    assertEquals(LocalDateTime.of(2025, 6, 3, 15, 37, 35), actual.getTime(), "AiredDate muss stimmen");
    assertEquals(Duration.ofSeconds(586), actual.getDuration(), "Dauer muss 586 Sekunden sein");
    assertEquals("https://www.kika.de/dein-song/zurueck-im-wettbewerb/videos/wie-wird-ein-song-zum-hit-102", actual.getWebsite().get().toString(), "Website muss stimmen");
    assertNull(actual.getAudioDescription(Resolution.SMALL), "AD SMALL");
    assertNull(actual.getAudioDescription(Resolution.NORMAL), "AD NORMAL");
    assertNull(actual.getAudioDescription(Resolution.HD), "AD HD");
    assertNull(actual.getSignLanguage(Resolution.SMALL), "DGS SMALL");
    assertNull(actual.getSignLanguage(Resolution.NORMAL), "DGS NORMAL");
    assertNull(actual.getSignLanguage(Resolution.HD), "DGS HD");
    assertEquals("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-087bde46997e_15.mp4", actual.getUrl(Resolution.WQHD).getUrl().toString(), "URL WQHD");
    assertEquals("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-6b00429f6b07_15.mp4", actual.getUrl(Resolution.SMALL).getUrl().toString(), "URL SMALL");
    assertEquals("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-31e0be270130_15.mp4", actual.getUrl(Resolution.NORMAL).getUrl().toString(), "URL NORMAL");
    assertEquals("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-5a2c8da1cdb7_15.mp4", actual.getUrl(Resolution.HD).getUrl().toString(), "URL HD");
    
  }
  
  private KikaFilmDto getInput() {
    List<KikaAssetDto> assets = asList(
        new KikaAssetDto(
            Optional.of("Video 2014 | MP4 Web L | 16:9 | 960x540"),
            Optional.of("FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-31e0be270130_15.mp4"),
            Optional.of(123839670),
            Optional.empty(),
            Optional.of(960),
            Optional.of(540),
            Optional.of(1800000),
            Optional.of(192000),
            Optional.of("progressive"),
            Optional.of("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-31e0be270130_15.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/webvtt")),

        new KikaAssetDto(
            Optional.of("Video 2018 | MP4 720p25 | Web XL| 16:9 | 1280x720"),
            Optional.of("FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-5a2c8da1cdb7_15.mp4"),
            Optional.of(217629946),
            Optional.empty(),
            Optional.of(1280),
            Optional.of(720),
            Optional.of(3584000),
            Optional.of(192000),
            Optional.of("progressive"),
            Optional.of("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-5a2c8da1cdb7_15.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/webvtt")),

        new KikaAssetDto(
            Optional.of("Video 2021 | MP4 1080p50 | Web XXL| 16:9 | 1920x1080"),
            Optional.of("FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-087bde46997e_15.mp4"),
            Optional.of(374732297),
            Optional.empty(),
            Optional.of(1920),
            Optional.of(1080),
            Optional.of(6656000),
            Optional.of(128000),
            Optional.of("progressive"),
            Optional.of("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-087bde46997e_15.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/webvtt")),

        new KikaAssetDto(
            Optional.of("Video 2014 | MP4 Web L mobil  | 16:9 | 640x360"),
            Optional.of("FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-6b00429f6b07_15.mp4"),
            Optional.of(82406704),
            Optional.empty(),
            Optional.of(640),
            Optional.of(360),
            Optional.of(1024000),
            Optional.of(192000),
            Optional.of("progressive"),
            Optional.of("https://kika-progressive.ard-mcdn.de/kika_de-prod/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-6b00429f6b07_15.mp4"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/webvtt")),

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
            Optional.of("https://kika-vod.ard-mcdn.de/hls-od/kika/online/mp4dyn/1/FCMS-15412fd3-42ca-444e-b4e0-e8a89e86c572-,6b00429f6b07,31e0be270130,5a2c8da1cdb7,087bde46997e,_15.mp4.csmil/master.m3u8"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/subtitle"),
            Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/webvtt"))
    );

    KikaFilmDto film = new KikaFilmDto(
        Optional.of("video"),                                             // docType
        Optional.of("wie-wird-ein-song-zum-hit-102"),                     // id
        Optional.of("ae0e5ad2-491d-4f0d-a74d-e4543bd0861d"),              // uuid (wird im Test ignoriert)
        Optional.of("VOID15412fd3-42ca-444e-b4e0-e8a89e86c572"),          // externalId
        Optional.of("/dein-song/zurueck-im-wettbewerb/videos"),           // urlPath
        Optional.of("wie-wird-ein-song-zum-hit-102"),                     // apiId
        Optional.of("https://www.kika.de/ackley/v1/videos/wie-wird-ein-song-zum-hit-102/assets"), // url
        Optional.of("2026-08-11T11:48:28.829+02:00"),                     // modificationDate
        Optional.of("2025-06-03T15:37:35.294+02:00"),                     // date
        Optional.of("4. Wie wird ein Song zum Hit?"),                     // title
        Optional.of("Lisa & Lukas feilen am Refrain und zeigen am goldenen Mikrofon welche Power ihre Stimmen haben. Und Paula wagt mit ihrem Waldhorn ein Experiment."), // teaserText
        Optional.of("Dein Song - Zurück im Wettbewerb"),                  // broadcastSeriesTitle
        Optional.of("2025-02-03T11:27:22.735+01:00"),                     // genDate
        Optional.of("Auf der Suche nach dem WOW-Moment arbeiten Lisa und Lukas am Refrain von \"Imperfections\" und zeigen Jules am goldenen Mikrofon, welche Power in ihren Stimmen steckt. Paula wagt ein Experiment: Mit ihrem Waldhorn will sie ihrem Song \"Escape\" noch einen ganz besonderen Sound geben. Und Sängerin LOTTE hat für die drei einen unschlagbaren Tipp."), // description
        Optional.of("4"),                                                 // episodeNumber
        Optional.of("586"),                                               // durationInSeconds
        Optional.of("1"));                                                // season

    film.setAssets(assets);

    return film;
  }
  
  
  protected KikaCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    return new KikaCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
