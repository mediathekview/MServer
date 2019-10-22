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
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL25kci5kZS8xNTcx\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi9mYjRlYTcwNC1lZTg4LTQ3M2MtYWEwNy1kOWY4Y2RmMjgyNTM\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9kM2MwNzcxZC1hNzgyLTRlOWItYWI3NC0zMTJjOTU2NWE0Y2Q\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2JhYnlsb24tYmVybGlu\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW53ZXR0ZXI\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi85YzhiYWE1Zi01ZDE0LTQwY2EtYjdjNC02NzAzNzNiYTUxOGQ\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
          new CrawlerUrlDTO(
              "https://api.ardmediathek.de/public-gateway?variables="
                  + URLEncoder.encode(
                      "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9icm9hZGNhc3RTZXJpZXM6L2JyZGUvZmVybnNlaGVuL2JheWVyaXNjaGVzLWZlcm5zZWhlbi9zZW5kdW5nZW4vemFtLXJvY2tlbi1ob2ZicmFldWhhdXM\",\"pageNumber\":0}")
                  + "&extensions="
                  + URLEncoder.encode(
                      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e\"}}")),
        };

    final ArdTopicsOverviewDeserializer instance = new ArdTopicsOverviewDeserializer();

    final Set<CrawlerUrlDTO> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}
