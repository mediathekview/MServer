package de.mediathekview.mserver.crawler.tagesschau.tasks;

import static org.junit.jupiter.api.Assertions.*;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauCrawler;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagesschauVideoTaskTest extends TagesschauTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testVideo() {
    final String requestUrl = "http://tagesschau-month.de";

    jsoupConnection = JsoupMock.mock(requestUrl, "/tagesschau/tagesschau_20jahre_video.html");
    final TagesschauCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final TagesschauVideoTask target = new TagesschauVideoTask(crawler, queue);
    final Set<Film> actual = target.invoke();
    assertEquals(1, actual.size());
    AssertFilm.assertEquals(
        actual.iterator().next(),
        Sender.TAGESSCHAU24,
        "tagesschau vor 20 Jahren",
        "tagesschau vor 20 Jahren, 30. Januar 2006",
        LocalDateTime.of(2006, 1, 30, 20, 0, 0),
        Duration.ofMinutes(15).plusSeconds(37),
        "",
        "https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547686.html",
        new GeoLocations[] {GeoLocations.GEO_NONE},
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webm.h264.mp4",
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webl.h264.mp4",
        "https://tagesschau-progressive.ard-mcdn.de/video/2026/0122/TV-20260122-1304-0500.webxl.h264.mp4",
        "");
  }
}
