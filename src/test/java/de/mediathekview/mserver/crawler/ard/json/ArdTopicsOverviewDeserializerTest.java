package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.net.URLEncoder;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArdTopicsOverviewDeserializerTest {

  @Test
  public void testDeserialize() {
    final JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topics.json");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL25kci5kZS8xNTcx\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi9mYjRlYTcwNC1lZTg4LTQ3M2MtYWEwNy1kOWY4Y2RmMjgyNTM\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9kM2MwNzcxZC1hNzgyLTRlOWItYWI3NC0zMTJjOTU2NWE0Y2Q\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2JhYnlsb24tYmVybGlu\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW53ZXR0ZXI\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi85YzhiYWE1Zi01ZDE0LTQwY2EtYjdjNC02NzAzNzNiYTUxOGQ\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9icm9hZGNhc3RTZXJpZXM6L2JyZGUvZmVybnNlaGVuL2JheWVyaXNjaGVzLWZlcm5zZWhlbi9zZW5kdW5nZW4vemFtLXJvY2tlbi1ob2ZicmFldWhhdXM\"}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"f97ef19f4425f287f4c73f94032233bc3f35a04ceb12a1a9d9a60a96ffc30636\"}}")),
        };

    final ArdTopicsOverviewDeserializer instance = new ArdTopicsOverviewDeserializer();

    final Set<CrawlerUrlDTO> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
