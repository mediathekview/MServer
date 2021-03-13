package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.FilmUrlInfoDto;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudVideoDetailsDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class NexxCloudVideoDetailsDeserializerTest {

  private final String jsonFile;
  private final Set<FilmUrlInfoDto> correctResults;
  private final MServerConfigManager rootConfig =
      new MServerConfigManager("MServer-JUnit-Config.yaml");

  public NexxCloudVideoDetailsDeserializerTest(
      final String jsonFile, final Set<FilmUrlInfoDto> correctResults) {
    this.jsonFile = jsonFile;
    this.correctResults = correctResults;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    final Set<FilmUrlInfoDto> videoDetails = new HashSet<>();

    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_320x180_400.mp4",
            320,
            180));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_640x360_700.mp4",
            640,
            360));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1024x576_1500.mp4",
            1024,
            576));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1280x720_2500.mp4",
            1280,
            720));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_1920x1080_6000.mp4",
            1920,
            1080));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_2560x1440_9000.mp4",
            2560,
            1440));
    videoDetails.add(
        new FilmUrlInfoDto(
            "https://funk-01dd.akamaized.net/b4fd4025-3285-4dc8-a0ee-53c5c967d347/1605930_src_3840x2160_16000.mp4",
            3840,
            2160));

    return new Object[][] {{"/funk/nexx_cloud_video_details.json", videoDetails}};
  }

  private FunkCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new FunkCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  @Test
  public void testDeserialize() throws URISyntaxException, IOException {
    final Type funkVideosType = new TypeToken<Set<FilmUrlInfoDto>>() {}.getType();
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                funkVideosType, new NexxCloudVideoDetailsDeserializer(createCrawler()))
            .create();

    final Set<FilmUrlInfoDto> videoResultList =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            funkVideosType);

    assertThat(videoResultList, equalTo(correctResults));
  }
}
