package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArdDayPageDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page1.json");

    final ArdFilmInfoDto[] expected =
        new ArdFilmInfoDto[] {
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvODQyZWNjZDItODYzNC00YzVjLTlkYTAtN2JmM2E2MDRmNTdi",
            ArdConstants.ITEM_URL + "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvODQyZWNjZDItODYzNC00YzVjLTlkYTAtN2JmM2E2MDRmNTdi",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzY5NzZiYTJlLTkyNWMtNDNhZi04NjQxLWJiMzZiMmY3YTkyMg",
            ArdConstants.ITEM_URL + "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzY5NzZiYTJlLTkyNWMtNDNhZi04NjQxLWJiMzZiMmY3YTkyMg",
              3),
          new ArdFilmInfoDto(
              "Y3JpZDovL3dkci5kZS9CZWl0cmFnLThhNDk4YjQxLWE3YmQtNDk3Yi1iNGRmLTdhMjdmZjcwNGZiYw",
            ArdConstants.ITEM_URL + "Y3JpZDovL3dkci5kZS9CZWl0cmFnLThhNDk4YjQxLWE3YmQtNDk3Yi1iNGRmLTdhMjdmZjcwNGZiYw",
              11),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy84MjlhNGY3OC1lNDRjLTQ2ZWItYTAxOS1kYmJjY2ZmMTkyNWMvMQ",
            ArdConstants.ITEM_URL + "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy84MjlhNGY3OC1lNDRjLTQ2ZWItYTAxOS1kYmJjY2ZmMTkyNWMvMQ",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2JyLmRlL3ZpZGVvL2E5NzY0NTJiLTI2YWUtNDJjOC05MThhLTFmZjczMjhmMWRiMw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2JyLmRlL3ZpZGVvL2E5NzY0NTJiLTI2YWUtNDJjOC05MThhLTFmZjczMjhmMWRiMw",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2JyLmRlL3ZpZGVvL2I4MzY5MzA5LTQyNmMtNGNmZi04OTcxLWRiM2QxNmMwMjc0YQ",
            ArdConstants.ITEM_URL + "Y3JpZDovL2JyLmRlL3ZpZGVvL2I4MzY5MzA5LTQyNmMtNGNmZi04OTcxLWRiM2QxNmMwMjc0YQ",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NTQyMQ",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS80NTQyMQ",
              7),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NTQzMQ",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS80NTQzMQ",
              3),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NDk0OQ",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS80NDk0OQ",
              1)
        };

    final ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    final Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
