package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
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
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvODQyZWNjZDItODYzNC00YzVjLTlkYTAtN2JmM2E2MDRmNTdi\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzY5NzZiYTJlLTkyNWMtNDNhZi04NjQxLWJiMzZiMmY3YTkyMg",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzY5NzZiYTJlLTkyNWMtNDNhZi04NjQxLWJiMzZiMmY3YTkyMg\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              3),
          new ArdFilmInfoDto(
              "Y3JpZDovL3dkci5kZS9CZWl0cmFnLThhNDk4YjQxLWE3YmQtNDk3Yi1iNGRmLTdhMjdmZjcwNGZiYw",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3dkci5kZS9CZWl0cmFnLThhNDk4YjQxLWE3YmQtNDk3Yi1iNGRmLTdhMjdmZjcwNGZiYw\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              11),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy84MjlhNGY3OC1lNDRjLTQ2ZWItYTAxOS1kYmJjY2ZmMTkyNWMvMQ",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy84MjlhNGY3OC1lNDRjLTQ2ZWItYTAxOS1kYmJjY2ZmMTkyNWMvMQ\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2JyLmRlL3ZpZGVvL2E5NzY0NTJiLTI2YWUtNDJjOC05MThhLTFmZjczMjhmMWRiMw",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2JyLmRlL3ZpZGVvL2E5NzY0NTJiLTI2YWUtNDJjOC05MThhLTFmZjczMjhmMWRiMw\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2JyLmRlL3ZpZGVvL2I4MzY5MzA5LTQyNmMtNGNmZi04OTcxLWRiM2QxNmMwMjc0YQ",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2JyLmRlL3ZpZGVvL2I4MzY5MzA5LTQyNmMtNGNmZi04OTcxLWRiM2QxNmMwMjc0YQ\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NTQyMQ",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2hyLW9ubGluZS80NTQyMQ\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              7),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NTQzMQ",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2hyLW9ubGluZS80NTQzMQ\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              3),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS80NDk0OQ",
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2hyLW9ubGluZS80NDk0OQ\",\"deviceType\":\"pc\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6\"}}"),
              1)
        };

    final ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    final Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
