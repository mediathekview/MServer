package de.mediathekview.mserver.crawler.kika.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiTopicPageDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class KikaApiTopicDeserializerTest {

  @ParameterizedTest
  @MethodSource("getDeserializeBrandTestArgumentSource")
  void testDeserializeBrand(
      final String jsonFile,
      final int numberOfExpectedRecords,
      final boolean hasSubpage,
      final boolean hasError)
      throws URISyntaxException, IOException {
    final Type KikaApiTopicDtoType = new TypeToken<Set<KikaApiTopicDto>>() {}.getType();
    //
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(KikaApiTopicDtoType, new KikaApiTopicPageDeserializer())
            .create();
    //
    final KikaApiTopicDto aKikaApiTopicDto =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            KikaApiTopicDtoType);
    //
    assertEquals(numberOfExpectedRecords, aKikaApiTopicDto.getElements().size());
    // has subpages
    assertEquals(hasSubpage, aKikaApiTopicDto.getNextPage().isPresent());
    // error page
    assertEquals(hasError, aKikaApiTopicDto.getErrorCode().isPresent());
    //
  }

  static Stream<Arguments> getDeserializeBrandTestArgumentSource() {
    return Stream.of(
        arguments("/kika/KikaApiTopic1.json", 10, true, false),
        arguments("/kika/KikaApiTopic2.json", 11, false, false),
        arguments("/kika/KikaApiError.json", 0, false, true));
  }
}
