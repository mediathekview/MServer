package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdErrorDeserializerTest {

  @Test
  public void deserializeTestNoError() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page1.json");

    final ArdErrorDeserializer target = new ArdErrorDeserializer();
    final Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(false));
  }

  @Test
  public void deserializeTestQueryNotFoundError() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_error_page_query_not_found.json");

    final ArdErrorInfoDto expected =
        new ArdErrorInfoDto("PERSISTED_QUERY_NOT_FOUND", "PersistedQueryNotFound");

    final ArdErrorDeserializer target = new ArdErrorDeserializer();
    final Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expected));
  }

  @Test
  public void deserializeTestInternalServerError() {
    final JsonElement jsonElement =
        JsonFileReader.readJson("/ard/ard_error_page_internal_server_error.json");

    final ArdErrorInfoDto expected =
        new ArdErrorInfoDto(
            "INTERNAL_SERVER_ERROR", "Cannot return null for non-nullable field Show.image.");

    final ArdErrorDeserializer target = new ArdErrorDeserializer();
    final Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expected));
  }
}
