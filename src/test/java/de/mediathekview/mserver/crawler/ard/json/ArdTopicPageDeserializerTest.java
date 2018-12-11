package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.net.URLEncoder;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ArdTopicPageDeserializerTest {
  @Test
  public void testDeserialize() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_topic.json");

    ArdFilmInfoDto[] expected = new ArdFilmInfoDto[]{
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhL2IxZjY2NWQzLWIyOTYtNDQ4ZS05YmQ1LTk5MWUzZTcxZWNkMQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhL2IxZjY2NWQzLWIyOTYtNDQ4ZS05YmQ1LTk5MWUzZTcxZWNkMQ\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzdkMWRhYzhmLWMxOWEtNDdjZS1hNDBmLTgzN2NjZTRhNTNjZQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzdkMWRhYzhmLWMxOWEtNDdjZS1hNDBmLTgzN2NjZTRhNTNjZQ\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhL2I0MWQ3MDhjLTMwNDItNGNhYy1iOTMyLTI0NzNlNjdiM2MwOQ",
            "https://api.ardmediathek.de/public-gateway?variables="+ URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhL2I0MWQ3MDhjLTMwNDItNGNhYy1iOTMyLTI0NzNlNjdiM2MwOQ\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhiMzE1ZTlkLWFmZjktNDE2Mi1iNzk1LWY3OTk3MGJjYWZkMw",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhiMzE1ZTlkLWFmZjktNDE2Mi1iNzk1LWY3OTk3MGJjYWZkMw\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzk2OTI1NTQxLWRkZTQtNGJhOS1hZTUyLTQ0MDUwYzIwOGVlOA",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzk2OTI1NTQxLWRkZTQtNGJhOS1hZTUyLTQ0MDUwYzIwOGVlOA\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhjYTk2MmFiLTgyOTctNDE4Yy05YzUxLTdlN2Y1Mjg4ZWY5ZS9hdWRpb2Rlc2tyaXB0aW9u",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhjYTk2MmFiLTgyOTctNDE4Yy05YzUxLTdlN2Y1Mjg4ZWY5ZS9hdWRpb2Rlc2tyaXB0aW9u\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            0)
    };

    ArdTopicPageDeserializer instance = new ArdTopicPageDeserializer();

    Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
}