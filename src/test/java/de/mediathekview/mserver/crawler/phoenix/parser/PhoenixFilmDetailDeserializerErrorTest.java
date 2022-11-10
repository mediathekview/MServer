package de.mediathekview.mserver.crawler.phoenix.parser;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PhoenixFilmDetailDeserializerErrorTest {

  @Test
  public void testNullInAbsatzArray() {
      final JsonElement jsonElement =
              JsonFileReader.readJson("/phoenix/phoenix_film_detail_null_item_in_array.json");
      final PhoenixFilmDetailDeserializer target = new PhoenixFilmDetailDeserializer();
      final Optional<PhoenixFilmDetailDto> actual = target.deserialize(jsonElement, null, null);
    assertThat(actual.isPresent(), equalTo(false));
  }
}
