package de.mediathekview.mserver.crawler.phoenix.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Optional;
import org.junit.Test;

public class PhoenixFilmDetailDeserializerTestError {

  @Test
  public void testNullInAbsatzArray() {
    JsonElement jsonElement = JsonFileReader.readJson("/phoenix/phoenix_film_detail_null_item_in_array.json");
    PhoenixFilmDetailDeserializer target = new PhoenixFilmDetailDeserializer();
    Optional<PhoenixFilmDetailDto> actual = target.deserialize(jsonElement, null, null);
    assertThat(actual.isPresent(), equalTo(false));
  }
}
