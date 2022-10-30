package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudSessionInitDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class NexxCloudSessionInitDeserializerTest {
  private final MServerConfigManager rootConfig =
      new MServerConfigManager("MServer-JUnit-Config.yaml");

  static Stream<Arguments> data() {
    return Stream.of(
        arguments("/funk/nexx_cloud_session_init.json", 3155618042501156672L),
        arguments("/funk/funk_video_page_last.json", null));
  }

  private FunkCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new FunkCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  @ParameterizedTest
  @MethodSource("data")
  void testDeserialize(String jsonFile, Long correctResults)
      throws URISyntaxException, IOException {
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(Long.class, new NexxCloudSessionInitDeserializer(createCrawler()))
            .create();

    final Long result =
        gson.fromJson(
            Files.newBufferedReader(
                Paths.get(Objects.requireNonNull(getClass().getResource(jsonFile)).toURI())),
            Long.class);

    assertThat(result).isEqualTo(correctResults);
  }
}
