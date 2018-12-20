package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.net.URLEncoder;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ArdDayPageDeserializerTest {

  @Test
  public void testDeserialize() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page1.json");

    ArdFilmInfoDto[] expected = new ArdFilmInfoDto[]{
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhjYTk2MmFiLTgyOTctNDE4Yy05YzUxLTdlN2Y1Mjg4ZWY5ZQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhjYTk2MmFiLTgyOTctNDE4Yy05YzUxLTdlN2Y1Mjg4ZWY5ZQ\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            4),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy9kNjEwODFkYS0xNWZiLTQ4YzYtOGNiOS1jMDlhN2E2MTA0MjQvMQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy9kNjEwODFkYS0xNWZiLTQ4YzYtOGNiOS1jMDlhN2E2MTA0MjQvMQ\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy83OWY5MzY0NC0yNWUyLTQ3YWItODc1OC1mN2QyZGQzNGYzZWQvMQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy83OWY5MzY0NC0yNWUyLTQ3YWItODc1OC1mN2QyZGQzNGYzZWQvMQ\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvNzAzN2ViOTAtYzU2ZS00NGM3LWJhZGItYTc0N2FlNTA1ODZh",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvNzAzN2ViOTAtYzU2ZS00NGM3LWJhZGItYTc0N2FlNTA1ODZh\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL3N3ci5kZS9hZXgvbzEwNzYyMTc",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder
                .encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3N3ci5kZS9hZXgvbzEwNzYyMTc\",\"deviceType\":\"pc\"}") + "&extensions="
                + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            3),
        new ArdFilmInfoDto("Y3JpZDovL3dkci5kZS9CZWl0cmFnLWEzNTQ3NzY4LTJmMzctNDA3NS1iYmU0LTVlMDgxY2Q3NDIxYw",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3dkci5kZS9CZWl0cmFnLWEzNTQ3NzY4LTJmMzctNDA3NS1iYmU0LTVlMDgxY2Q3NDIxYw\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            5),
        new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS9jMGM0MzIxMS0yNjI3LTRhNjgtODc1Ny1iZTQzYzBkYWQ3NWE",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS9jMGM0MzIxMS0yNjI3LTRhNjgtODc1Ny1iZTQzYzBkYWQ3NWE\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzRiYjBkMGM2LTA0ZWEtNDdhMS1iNDI3LWZiYTdkMTQ4Mzg5Yw",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzRiYjBkMGM2LTA0ZWEtNDdhMS1iNDI3LWZiYTdkMTQ4Mzg5Yw\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL2JyLmRlL3ZpZGVvL2VmN2JhODUyLTI3MGItNDcyZS04YTdjLTJhYzBlZWJmYzMxZQ",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2JyLmRlL3ZpZGVvL2VmN2JhODUyLTI3MGItNDcyZS04YTdjLTJhYzBlZWJmYzMxZQ\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            1),
        new ArdFilmInfoDto("Y3JpZDovL3dkci5kZS9CZWl0cmFnLTA5MzA1YTVlLTNmOGItNDg4ZC05ZDY3LWJmM2FhMDFmN2E0MA",
            "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode(
                "{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3dkci5kZS9CZWl0cmFnLTA5MzA1YTVlLTNmOGItNDg4ZC05ZDY3LWJmM2FhMDFmN2E0MA\",\"deviceType\":\"pc\"}")
                + "&extensions=" + URLEncoder.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            4)
    };

    ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    Set<ArdFilmInfoDto> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }

}
