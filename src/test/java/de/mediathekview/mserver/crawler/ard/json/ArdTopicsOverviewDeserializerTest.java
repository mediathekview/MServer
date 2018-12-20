package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.net.URLEncoder;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ArdTopicsOverviewDeserializerTest {

  @Test
  public void testDeserialize() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topics.json");

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder
                .encode("{\"client\":\"ard\",\"showId\":\"Y3JpZDovL25kci5kZS8xNTcx\"}") + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi9mYjRlYTcwNC1lZTg4LTQ3M2MtYWEwNy1kOWY4Y2RmMjgyNTM\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9kM2MwNzcxZC1hNzgyLTRlOWItYWI3NC0zMTJjOTU2NWE0Y2Q\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder
                .encode("{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2JhYnlsb24tYmVybGlu\"}") + "&extensions=" + URLEncoder
                .encode(
                    "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder
                .encode("{\"client\":\"ard\",\"showId\":\"Y3JpZDovL3JhZGlvYnJlbWVuLmRlL2J1dGVudW5iaW5uZW53ZXR0ZXI\"}") + "&extensions="
                + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi85YzhiYWE1Zi01ZDE0LTQwY2EtYjdjNC02NzAzNzNiYTUxOGQ\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
        new CrawlerUrlDTO(
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"showId\":\"Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9icm9hZGNhc3RTZXJpZXM6L2JyZGUvZmVybnNlaGVuL2JheWVyaXNjaGVzLWZlcm5zZWhlbi9zZW5kdW5nZW4vemFtLXJvY2tlbi1ob2ZicmFldWhhdXM\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"1f680c1618207fa89687afcdac128bd15f6923b5d1fef57fdd30aac716b9239e\"}}")
        ),
    };

    ArdTopicsOverviewDeserializer instance = new ArdTopicsOverviewDeserializer();

    Set<CrawlerUrlDTO> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}