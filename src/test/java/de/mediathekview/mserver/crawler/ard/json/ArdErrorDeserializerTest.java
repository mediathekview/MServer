package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Optional;
import org.junit.Test;

public class ArdErrorDeserializerTest {

  @Test
  public void deserializeTestNoError() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page1.json");

    ArdErrorDeserializer target = new ArdErrorDeserializer();
    Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(false));
  }

  @Test
  public void deserializeTestQueryNotFoundError() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_error_page_query_not_found.json");

    ArdErrorInfoDto expected = new ArdErrorInfoDto("PERSISTED_QUERY_NOT_FOUND", "PersistedQueryNotFound");

    ArdErrorDeserializer target = new ArdErrorDeserializer();
    Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expected));
  }

  @Test
  public void deserializeTestInternalServerError() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_error_page_internal_server_error.json");

    ArdErrorInfoDto expected = new ArdErrorInfoDto("INTERNAL_SERVER_ERROR", "Cannot return null for non-nullable field Show.image.");

    ArdErrorDeserializer target = new ArdErrorDeserializer();
    Optional<ArdErrorInfoDto> actual = target.deserialize(jsonElement, null, null);

    assertThat(actual.isPresent(), equalTo(true));
    assertThat(actual.get(), equalTo(expected));
  }
}