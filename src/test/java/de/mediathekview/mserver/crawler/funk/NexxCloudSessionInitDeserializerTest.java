package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.funk.json.NexxCloudSessionInitDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class NexxCloudSessionInitDeserializerTest {

  private final String jsonFile;
  private final Long correctResults;
  private final MServerConfigManager rootConfig =
      new MServerConfigManager("MServer-JUnit-Config.yaml");

  public NexxCloudSessionInitDeserializerTest(final String jsonFile, final Long correctResults) {
    this.jsonFile = jsonFile;
    this.correctResults = correctResults;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[][] {
      {"/funk/nexx_cloud_session_init.json", 3155618042501156672L},
      {"/funk/funk_video_page_last.json", null}
    };
  }

  private FunkCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new FunkCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  @Test
  public void testDeserialize() throws URISyntaxException, IOException {
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(Long.class, new NexxCloudSessionInitDeserializer(createCrawler()))
            .create();

    final Long result =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            Long.class);

    assertThat(result, equalTo(correctResults));
  }
}
