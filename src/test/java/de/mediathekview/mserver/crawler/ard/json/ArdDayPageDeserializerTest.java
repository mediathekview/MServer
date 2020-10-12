package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdDayPageDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonArray jsonElement = JsonFileReader.readJsonArray("/ard/ard_day_page11.json");

    final ArdFilmInfoDto[] expected =
        new ArdFilmInfoDto[] {
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5Nzc",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5Nzc",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5Nzg",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5Nzg",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5ODI",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5ODI",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE4MjA",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE4MjA",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDEyMDM",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDEyMDM",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5OTI",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5OTI",
              7),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5NzI",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5NzI",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8xMDE5NzE",
              ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8xMDE5NzE",
              1)
        };

    final ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    final Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
