package de.mediathekview.mserver.crawler.kika;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;

import de.mediathekview.mserver.crawler.kika.tasks.KikaVideoSubchannelPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


class KikaVideoSubchannelPageTaskTest extends WireMockTestBase {
  
  @Test
  void testAssetExtraction() {
    setupSuccessfulJsonResponse("/KikaVideoSubchannel", "/kika/KikaVideoSubchannel.json");
    Set<KikaFilmDto> result = executeTask(999, "/KikaVideoSubchannel");
    List<KikaFilmDto> expectedResult = generateExpectedResult();

    assertEquals(expectedResult.size(), result.size(), "Number of parsed films must match");

    List<KikaFilmDto> actualSorted = result.stream()
        .sorted(Comparator.comparing(f -> ((KikaFilmDto) f).getUrl()))
        .collect(Collectors.toList());
    List<KikaFilmDto> expectedSorted = expectedResult.stream()
        .sorted(Comparator.comparing(f -> ((KikaFilmDto) f).getUrl()))
        .collect(Collectors.toList());

    for (int i = 0; i < expectedSorted.size(); i++) {
      KikaFilmDto expected = expectedSorted.get(i);
      KikaFilmDto actual = actualSorted.get(i);

      assertEquals(expected.getDocType(), actual.getDocType(), "docType must match at index " + i);
      assertEquals(expected.getId(), actual.getId(), "id must match at index " + i);
      assertEquals(expected.getUuid(), actual.getUuid(), "uuid must match at index " + i);
      assertEquals(expected.getExternalId(), actual.getExternalId(), "externalId must match at index " + i);
      assertEquals(expected.getUrlPath(), actual.getUrlPath(), "urlPath must match at index " + i);
      assertEquals(expected.getApiId(), actual.getApiId(), "apiId must match at index " + i);
      assertEquals(expected.getUrl(), actual.getUrl(), "url must match at index " + i);
      assertEquals(expected.getModificationDate(), actual.getModificationDate(), "modificationDate must match at index " + i);
      assertEquals(expected.getDate(), actual.getDate(), "date must match at index " + i);
      assertEquals(expected.getTitle(), actual.getTitle(), "title must match at index " + i);
      assertEquals(expected.getTeaserText(), actual.getTeaserText(), "teaserText must match at index " + i);
      assertEquals(expected.getBroadcastSeriesTitle(), actual.getBroadcastSeriesTitle(), "broadcastSeriesTitle must match at index " + i);
      assertEquals(expected.getGenDate(), actual.getGenDate(), "genDate must match at index " + i);
      assertEquals(expected.getDescription(), actual.getDescription(), "description must match at index " + i);
      assertEquals(expected.getEpisodeNumber(), actual.getEpisodeNumber(), "episodeNumber must match at index " + i);
      assertEquals(expected.getDurationInSeconds(), actual.getDurationInSeconds(), "durationInSeconds must match at index " + i);
      assertEquals(expected.getSeason(), actual.getSeason(), "season must match at index " + i);
    }
  }
  
  @Test
  void testExtractFilmsWithPagingLimit() {
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=0&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel0.json");
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=1&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel1.json");
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=2&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel2.json");
    Set<KikaFilmDto> result = executeTask(2, "/ackley/v1/videosubchannels/alle-folgen-938/videos?page=0&videoType=mainContent&platform=kikade");
    assertEquals(24, result.size(), "Number of extracted entities per letter");
  }
  
  @Test
  void testExtractAllFilms() {
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=0&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel0.json");
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=1&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel1.json");
    setupSuccessfulJsonResponse("/ackley/v1/videosubchannels/alle-folgen-938/videos?page=2&videoType=mainContent&platform=kikade", "/kika/KikaVideoSubchannel2.json");
    Set<KikaFilmDto> result = executeTask(999, "/ackley/v1/videosubchannels/alle-folgen-938/videos?page=0&videoType=mainContent&platform=kikade");
    assertEquals(36, result.size(), "Number of extracted entities per letter");
  }
  
  
  private Set<KikaFilmDto> executeTask(int limit, String... requestUrl) {
    final Queue<KikaEntityDto> input = new ConcurrentLinkedQueue<>();
    for (String url : requestUrl) {
      input.add(new KikaEntityDto(
          Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),Optional.empty(),
          Optional.of(getWireMockBaseUrlSafe() + url),
          Optional.empty()
      ));
    }
    return new KikaVideoSubchannelPageTask(createCrawler(), input, limit).invoke();
  }
  
  private List<KikaFilmDto> generateExpectedResult() {
    return new ArrayList<>(asList(
        new KikaFilmDto(
            Optional.of("video"),                                                              // docType
            Optional.of("zweihundertzweiunddreissig-normans-witzige-welt-der-erwachsenen-102"), // id
            Optional.of("3bc65ac6-315d-4ef5-9e28-b2535ae6ac3a"),                                // uuid
            Optional.of("VOID695eec05-4213-416a-82fb-d0dd2ff9e4e6"),                            // externalId
            Optional.of("/feuerwehrmann-sam/videos"),                                           // urlPath
            Optional.of("zweihundertzweiunddreissig-normans-witzige-welt-der-erwachsenen-102"), // apiId
            Optional.of("https://www.kika.de/ackley/v1/videos/zweihundertzweiunddreissig-normans-witzige-welt-der-erwachsenen-102/assets"), // url
            Optional.of("2026-08-09T18:20:09.696+02:00"),                                       // modificationDate
            Optional.of("2026-08-09T18:15:03.778+02:00"),                                       // date
            Optional.of("Normans witzige Welt der Erwachsenen"),                                // title
            Optional.of("Für Normans neuen Livestream versuchen Hannah und Norman, dass Joe witzige Sachen vor der Kamera macht, aber das geht mächtig schief."), // teaserText
            Optional.of("Feuerwehrmann Sam"),                                                   // broadcastSeriesTitle
            Optional.of("2026-08-09T19:30:51.383+02:00"),                                       // genDate
            Optional.of("Dereks Livestream über die witzige Welt der Hunde ist ein voller Erfolg. Das wurmt Norman, denn sein Livestream über die Geschichte der Gewürzgurke hat kaum Likes. Hannah schlägt ihm vor, ihren Vater bei seinen Experimenten zu filmen. Bei denen geht immer etwas schief und sowas kommt im Netz immer gut an. Aber als Joes raketengetriebenes Hovercraft außer Kontrolle gerät, wird aus Spaß plötzlich Ernst."), // description
            Optional.of("232"),                                                                 // episodeNumber
            Optional.of("568"),                                                                 // durationInSeconds
            Optional.of("15")),                                                                 // season

        new KikaFilmDto(
            Optional.of("video"),
            Optional.of("zweihunderteinunddreissig-annies-tier-parade-102"),
            Optional.of("b0f285e1-e4d7-43ac-be67-d03f4efbcd4a"),
            Optional.of("VOID938d0161-914f-4208-aabf-aa7da6a1f731"),
            Optional.of("/feuerwehrmann-sam/videos"),
            Optional.of("zweihunderteinunddreissig-annies-tier-parade-102"),
            Optional.of("https://www.kika.de/ackley/v1/videos/zweihunderteinunddreissig-annies-tier-parade-102/assets"),
            Optional.of("2026-08-08T18:20:09.734+02:00"),
            Optional.of("2026-08-08T18:15:04.786+02:00"),
            Optional.of("Annies Tier-Parade"),
            Optional.of("Auf Annies Farm findet die große Tierparade statt bei. Peter und Norman habe zwar keine eigenen Tiere, wollen aber mit Wollie und Lämmchen antreten."),
            Optional.of("Feuerwehrmann Sam"),
            Optional.of("2026-08-09T19:30:51.435+02:00"),
            Optional.of("Auf Annies Bauernhof findet eine große Tierparade statt. Alle sind mit ihren Lieblingen da: Elvis mit Schnuffi, James mit Tiger, Gareth mit Buddler, Feuerwehrhauptmann Steele mit Meerschweinchen Norris und Polizeihauptmeisterin Ravani mit Kilo. Peter, Norman und Hannah würden auch gern mitmachen, nur leider haben sie kein Tier. Da kommt Peter auf die unvernünftige Idee, Wollie und Lämmchen von der Wiese zu holen."),
            Optional.of("231"),
            Optional.of("568"),
            Optional.of("15")),

        new KikaFilmDto(
            Optional.of("video"),
            Optional.of("einhundertsiebenundsiebzig-der-ausgefuchste-fuchs-100"),
            Optional.of("483a0837-7ede-4e54-932e-534c609a7a0d"),
            Optional.of("VOIDd5a2be39-8911-47f0-bdb2-4ac958be4c8d"),
            Optional.of("/feuerwehrmann-sam/videos"),
            Optional.of("einhundertsiebenundsiebzig-der-ausgefuchste-fuchs-100"),
            Optional.of("https://www.kika.de/ackley/v1/videos/einhundertsiebenundsiebzig-der-ausgefuchste-fuchs-100/assets"),
            Optional.of("2026-08-09T18:20:12.726+02:00"),
            Optional.of("2026-08-09T18:15:04.144+02:00"),
            Optional.of("Der ausgefuchste Fuchs"),
            Optional.of("Die Jungen Retter sollen bei einem Dreibeinlauf Teamwork lernen. Das ist auch schon bald gegfragt, denn im Wald bricht ein Feuer aus!"),
            Optional.of("Feuerwehrmann Sam"),
            Optional.of("2026-08-09T19:30:51.352+02:00"),
            Optional.of("Die Jungen Retter sollen bei einem Dreibeinlauf lernen, wie man durch Teamwork ein Rennen gewinnt. Währenddessen sind Malcolm, Moose und Tom im Wald als \"die Wilden Männer von Pontypandy\" unterweg. Dort wollen sie ein richtiges Festmahl am Lagerfeuer zelebrieren. Aber ein überaus schlauer Fuchs jagt ihnen alle Vorräte ab. In der Aufregung wird aus der kleinen Feuerstelle plötzlich ein riesiger Waldbrand. Jetzt ist Teamwork gefragt."),
            Optional.of("177"),
            Optional.of("570"),
            Optional.of("13"))
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
