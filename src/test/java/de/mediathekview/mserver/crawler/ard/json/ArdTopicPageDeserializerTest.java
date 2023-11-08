package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.ard.ArdTopicInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdTopicPageDeserializerTest {
  @Test
  public void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topic.json");

      ArdFilmInfoDto[] expected =
              new ArdFilmInfoDto[]{
                      new ArdFilmInfoDto(
                              "Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzFmOTkwMzNlLWY1MjUtNDk4Yy1iZjQ5LWIwZjZjYjhjNzRkYQ",
                              "https://api.ardmediathek.de/page-gateway/pages/ard/item/Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzFmOTkwMzNlLWY1MjUtNDk4Yy1iZjQ5LWIwZjZjYjhjNzRkYQ",
                              0),
                      new ArdFilmInfoDto(
                              "Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzkzM2QzZjFjLTlkMWQtNGFlNy1hNDA5LTlhMjdjNGI0NTdjZA",
                          "https://api.ardmediathek.de/page-gateway/pages/ard/item/Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzkzM2QzZjFjLTlkMWQtNGFlNy1hNDA5LTlhMjdjNGI0NTdjZA",
                              0),
                      new ArdFilmInfoDto(
                              "Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzg1NTVhNjlmLWNkMDMtNDY3ZS04MWJmLWU3YmI4YjIxNGJjMw",
                          "https://api.ardmediathek.de/page-gateway/pages/ard/item/Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzg1NTVhNjlmLWNkMDMtNDY3ZS04MWJmLWU3YmI4YjIxNGJjMw",
                              0),
                      new ArdFilmInfoDto(
                              "Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2FhNDRiYTVhLWMzOWItNDgyMy1iNDZjLTZhMjBlYTAyN2I0ZQ",
                          "https://api.ardmediathek.de/page-gateway/pages/ard/item/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2FhNDRiYTVhLWMzOWItNDgyMy1iNDZjLTZhMjBlYTAyN2I0ZQ",
                              0),
                      new ArdFilmInfoDto(
                              "Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2M2MzU2NTJjLWVkYTAtNDNkNS05ZTJkLTliMzBkZTI2NzM2Mw",
                          "https://api.ardmediathek.de/page-gateway/pages/ard/item/Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2M2MzU2NTJjLWVkYTAtNDNkNS05ZTJkLTliMzBkZTI2NzM2Mw",
                              0),
              };

    final ArdTopicPageDeserializer instance = new ArdTopicPageDeserializer();

    final ArdTopicInfoDto ardTopicInfoDto = instance.deserialize(jsonElement, null, null);
    final Set<ArdFilmInfoDto> filmInfos = ardTopicInfoDto.getFilmInfos();

    assertThat(filmInfos.size(), equalTo(expected.length));
    assertThat(filmInfos, Matchers.containsInAnyOrder(expected));
  }
}
