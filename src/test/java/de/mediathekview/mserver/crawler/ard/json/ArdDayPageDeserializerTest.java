package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class ArdDayPageDeserializerTest {
  
  @Test
  public void testDeserialize() {
    JsonElement jsonElement = JsonFileReader.readJson("/ard/ard_day_page1.json");
    
    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[] {
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL2Flbm5hIGJ1cmRhLzhjYTk2MmFiLTgyOTctNDE4Yy05YzUxLTdlN2Y1Mjg4ZWY5ZQ\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy9kNjEwODFkYS0xNWZiLTQ4YzYtOGNiOS1jMDlhN2E2MTA0MjQvMQ\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RzMTAwcy83OWY5MzY0NC0yNWUyLTQ3YWItODc1OC1mN2QyZGQzNGYzZWQvMQ\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvNzAzN2ViOTAtYzU2ZS00NGM3LWJhZGItYTc0N2FlNTA1ODZh\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3N3ci5kZS9hZXgvbzEwNzYyMTc\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3dkci5kZS9CZWl0cmFnLWEzNTQ3NzY4LTJmMzctNDA3NS1iYmU0LTVlMDgxY2Q3NDIxYw\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS9jMGM0MzIxMS0yNjI3LTRhNjgtODc1Ny1iZTQzYzBkYWQ3NWE\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzRiYjBkMGM2LTA0ZWEtNDdhMS1iNDI3LWZiYTdkMTQ4Mzg5Yw\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2JyLmRlL3ZpZGVvL2VmN2JhODUyLTI3MGItNDcyZS04YTdjLTJhYzBlZWJmYzMxZQ\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
      new CrawlerUrlDTO("https://api.ardmediathek.de/public-gateway?variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL3dkci5kZS9CZWl0cmFnLTA5MzA1YTVlLTNmOGItNDg4ZC05ZDY3LWJmM2FhMDFmN2E0MA\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}")
    };

    ArdDayPageDeserializer instance = new ArdDayPageDeserializer();

    Set<CrawlerUrlDTO> result = instance.deserialize(jsonElement, null, null);

    assertThat(result.size(), equalTo(expected.length));
    assertThat(result, Matchers.containsInAnyOrder(expected));
  }
  
}
