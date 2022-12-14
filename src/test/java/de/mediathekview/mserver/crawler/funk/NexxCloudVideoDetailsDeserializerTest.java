package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.FilmUrlInfoDto;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudVideoDetailsDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NexxCloudVideoDetailsDeserializerTest {

  private final MServerConfigManager rootConfig =
      new MServerConfigManager("MServer-JUnit-Config.yaml");

  static Stream<Arguments> data() {

    final String filenameAzure = "/funk/nexx_cloud_video_details.json";
    final Set<FilmUrlInfoDto> videoDetailsAzure = new HashSet<>();

    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_320x180_400.mp4",
            320,
            180));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_640x360_700.mp4",
            640,
            360));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1024x576_1500.mp4",
            1024,
            576));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1280x720_2500.mp4",
            1280,
            720));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1920x1080_6000.mp4",
            1920,
            1080));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_2560x1440_9000.mp4",
            2560,
            1440));
    videoDetailsAzure.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_3840x2160_16000.mp4",
            3840,
            2160));
    // 3Q
    final String filename3q = "/funk/nexx_cloud_video_details_3q.json";
    final Set<FilmUrlInfoDto> videoDetails3q = new HashSet<>();

    videoDetails3q.add(
        new FilmUrlInfoDto(
            "https://funk-02.akamaized.net/22679/files/21/03/22/3044074/5-GmkJ6y3hHMvqNfC9x8gr.mp4",
            9,
            8));
    videoDetails3q.add(
        new FilmUrlInfoDto(
            "https://funk-02.akamaized.net/22679/files/21/03/22/3044074/4-tb6Q8DjFGgdhLr2PvMTX.mp4",
            640,
            360));
    videoDetails3q.add(
        new FilmUrlInfoDto(
            "https://funk-02.akamaized.net/22679/files/21/03/22/3044074/32-MrHvQRmn63Tq4BkzyYZw.mp4",
            1024,
            576));
    videoDetails3q.add(
        new FilmUrlInfoDto(
            "https://funk-02.akamaized.net/22679/files/21/03/22/3044074/2-djtFmfnWpYKBXrc3PThL.mp4",
            1280,
            720));
    videoDetails3q.add(
        new FilmUrlInfoDto(
            "https://funk-02.akamaized.net/22679/files/21/03/22/3044074/1-Y9RWVKQ2GLhXgm763CnB.mp4",
            1920,
            1080));

    return Stream.of(
        arguments(filenameAzure, videoDetailsAzure), arguments(filename3q, videoDetails3q));
  }

  private FunkCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new FunkCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeserialize(String jsonFile, Set<FilmUrlInfoDto> correctResults)
      throws URISyntaxException, IOException {
    final Type funkVideosType = new TypeToken<Set<FilmUrlInfoDto>>() {}.getType();
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                funkVideosType, new NexxCloudVideoDetailsDeserializer(createCrawler()))
            .create();

    final Set<FilmUrlInfoDto> videoResultList =
        gson.fromJson(
            Files.newBufferedReader(
                Paths.get(Objects.requireNonNull(getClass().getResource(jsonFile)).toURI())),
            funkVideosType);

    assertThat(videoResultList).isEqualTo(correctResults);
  }
}
