package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArdDayPageDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonArray jsonElement = JsonFileReader.readJsonArray("/ard/ard_day_page11.json");

    final ArdFilmInfoDto[] expected =
        new ArdFilmInfoDto[] {
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI3N18yMDIwLTA3LTA1VDEwOjA1",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI3N18yMDIwLTA3LTA1VDEwOjA1",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIyMDA0N18yMDIwLTA3LTA1VDExOjQw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIyMDA0N18yMDIwLTA3LTA1VDExOjQw",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI3N18yMDIwLTA3LTA1VDEyOjMw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI3N18yMDIwLTA3LTA1VDEyOjMw",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI2OV8yMDIwLTA3LTA1VDE0OjQ1",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI2OV8yMDIwLTA3LTA1VDE0OjQ1",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8wMDAwMDEyMF8yMDIwLTA3LTA1VDE5OjAw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8wMDAwMDEyMF8yMDIwLTA3LTA1VDE5OjAw",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODAzNzQ0NF8yMDIwLTA3LTA1VDE5OjMw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODAzNzQ0NF8yMDIwLTA3LTA1VDE5OjMw",
              7),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI1N18yMDIwLTA3LTA1VDIxOjQ1",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIxMDI1N18yMDIwLTA3LTA1VDIxOjQ1",
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2hyLW9ubGluZS8zODIyMDA0M18yMDIwLTA3LTA2VDAwOjAw",
            ArdConstants.ITEM_URL + "Y3JpZDovL2hyLW9ubGluZS8zODIyMDA0M18yMDIwLTA3LTA2VDAwOjAw",
              1)
        };

    final ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    final Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
