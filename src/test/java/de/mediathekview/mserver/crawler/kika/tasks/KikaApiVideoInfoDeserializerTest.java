package de.mediathekview.mserver.crawler.kika.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.kika.KikaApiCrawler;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoPageDeserializer;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;


@RunWith(Parameterized.class)
public class KikaApiVideoInfoDeserializerTest {

  private String jsonFile = "";
  private int numberOfExpectedRecords = 0;
  private boolean hasError = false;

  public KikaApiVideoInfoDeserializerTest(String jsonFile, int numberOfExpectedRecords, boolean error) {
    this.jsonFile = jsonFile;
    this.numberOfExpectedRecords = numberOfExpectedRecords;
    this.hasError = error;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    return new Object[][] {
          {
            "/kika/KikaApiFilm1.json",
            3,
            false
          },
          {
            "/kika/KikaApiFilm2.json",
            3,
            false
          },
          {
            "/kika/KikaApiError.json",
            0,
            true
          }
        };

  }

  @Test
  public void testDeserializeBrand() throws URISyntaxException, IOException {
    final Type kikaApiVideoInfoDtoType = new TypeToken<Set<KikaApiVideoInfoDto>>() {}.getType();
    //
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(kikaApiVideoInfoDtoType, new KikaApiVideoInfoPageDeserializer(createCrawler()))
            .create();
    //
    final KikaApiVideoInfoDto aKikaApiVideoDto =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            kikaApiVideoInfoDtoType);
    //
    assertEquals(numberOfExpectedRecords, aKikaApiVideoDto.getVideoUrls().size());
    // error page
    assertEquals(hasError, aKikaApiVideoDto.getErrorCode().isPresent());
    //
  }
  
  protected KikaApiCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new KikaApiCrawler(forkJoinPool, nachrichten, fortschritte, MServerConfigManager.getInstance("MServer-JUnit-Config.yaml"));
  }
}
