package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdFilmDeserializerErrorTest {

  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"/ard/ard_film_page_fsk_day11.json"}, {"/ard/ard_film_page_no_video11.json"}
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void testFilmNoVideo(final String jsonFile) {

    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    final ArdFilmDeserializer target = new ArdFilmDeserializer(createCrawler());
    final List<ArdFilmDto> actualFilms = target.deserialize(jsonElement, null, null);

    assertThat(actualFilms.size(), equalTo(0));
  }

  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
