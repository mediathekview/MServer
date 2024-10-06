package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page.json");

    final ArdFilmInfoDto[] expected =
        new ArdFilmInfoDto[] {
          new ArdFilmInfoDto(
              "Y3JpZDovL3JiYl8xY2RjODJjMy01ZTIyLTQ0MDctODEwZi0yMWMwYTBhY2NjMmNfcHVibGljYXRpb24",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL3JiYl8xY2RjODJjMy01ZTIyLTQ0MDctODEwZi0yMWMwYTBhY2NjMmNfcHVibGljYXRpb24"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL3JiYl9hN2RkMDNjMC0yMmU5LTRmYzEtYmNiOC1kYTg0Y2RjOWMxMWZfcHVibGljYXRpb24",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL3JiYl9hN2RkMDNjMC0yMmU5LTRmYzEtYmNiOC1kYTg0Y2RjOWMxMWZfcHVibGljYXRpb24"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL21kci5kZS9zZW5kdW5nLzI4MjA0MC80MDQ4MzMtMzg1Mjgw",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL21kci5kZS9zZW5kdW5nLzI4MjA0MC80MDQ4MzMtMzg1Mjgw"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL21kci5kZS9zZW5kdW5nLzI4MjA0MC80MDQ4MzQtMzg1Mjgx",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL21kci5kZS9zZW5kdW5nLzI4MjA0MC80MDQ4MzQtMzg1Mjgx"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2Zlcm5zZWhmaWxtZSBpbSBlcnN0ZW4vMjAyNC0wOS0yOF8xNC0wMC1NRVNa",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2Zlcm5zZWhmaWxtZSBpbSBlcnN0ZW4vMjAyNC0wOS0yOF8xNC0wMC1NRVNa"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2Zlcm5zZWhmaWxtZSBpbSBlcnN0ZW4vMjAyNC0wOS0yOF8xNS0zMC1NRVNa",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2Zlcm5zZWhmaWxtZSBpbSBlcnN0ZW4vMjAyNC0wOS0yOF8xNS0zMC1NRVNa"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL3dkci5kZS9CZWl0cmFnLXNvcGhvcmEtMmIwZDg4NDMtMzQ0YS00OTZmLTlhNDYtNGY3ODk5MjE2MmFi",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL3dkci5kZS9CZWl0cmFnLXNvcGhvcmEtMmIwZDg4NDMtMzQ0YS00OTZmLTlhNDYtNGY3ODk5MjE2MmFi"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2RpZS1zdGlsbGVuLW1vZXJkZXIvMjAyNC0wOS0yOF8yMC0xNS1NRVNa",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2RpZS1zdGlsbGVuLW1vZXJkZXIvMjAyNC0wOS0yOF8yMC0xNS1NRVNa"),
              1),
          new ArdFilmInfoDto(
              "Y3JpZDovL2Rhc2Vyc3RlLmRlL2hhcnR3aWctc2VlbGVyLzIwMjQtMDktMjhfMjEtNDUtTUVTWg",
              String.format(
                  ArdConstants.ITEM_URL,
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2hhcnR3aWctc2VlbGVyLzIwMjQtMDktMjhfMjEtNDUtTUVTWg"),
              1)
        };

    final ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    final Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
