package de.mediathekview.mserver.crawler.ard.json;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Set;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class ArdTopicsDeserializerTest {
  @Test
  void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topic_page_sender_overview.json");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uYg?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uZA?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uZQ?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uZg?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uaA?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uag?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4ubA?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4ubg?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4ucA?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4ucg?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4udA?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4udQ?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4udg?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4udw?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4ueQ?pageNumber=0&pageSize=200"),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/page-gateway/widgets/radiobremen/editorials/UmFkaW9CcmVtZW4uIw?pageNumber=0&pageSize=200")
        };

    final ArdTopicsDeserializer target = new ArdTopicsDeserializer("radiobremen");
    final Set<CrawlerUrlDTO> actual = target.deserialize(jsonElement, null, null);

    assertEquals(expected.length, actual.size());
    MatcherAssert.assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
