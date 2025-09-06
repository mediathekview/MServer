package de.mediathekview.mserver.crawler.kika.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoDto;
import de.mediathekview.mserver.crawler.kika.json.KikaApiVideoInfoPageDeserializer;
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

public class KikaApiVideoInfoDeserializerTest {

  @ParameterizedTest
  @MethodSource("getDeserializeBrandTestArgumentSource")
  public void testDeserializeBrand(
      final String jsonFile, final int numberOfExpectedRecords, final boolean hasError)
      throws URISyntaxException, IOException {
    final Type kikaApiVideoInfoDtoType = new TypeToken<KikaApiVideoInfoDto>() {}.getType();
    //
    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(kikaApiVideoInfoDtoType, new KikaApiVideoInfoPageDeserializer())
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

  static Stream<Arguments> getDeserializeBrandTestArgumentSource() {
    return Stream.of(
        arguments("/kika/KikaApiFilm1.json", 3, false),
        arguments("/kika/KikaApiFilm2.json", 3, false),
        arguments("/kika/KikaApiError.json", 0, true));
  }
}
