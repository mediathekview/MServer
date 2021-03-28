package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class OrfFilmDetailTaskJugendschutzTest extends OrfFilmDetailTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testJugendschutzNotAdded() throws IOException {
    setupHeadRequestForFileSize();
    final String requestUrl =
        "https://tvthek.orf.at/profile/Tatort/2713749/Tatort-Hetzjagd/14081980";
    jsoupConnection = JsoupMock.mock(requestUrl, "/orf/orf_film_jugendschutz.html");
    OrfCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final Set<Film> actual = executeTask(crawler, "Tatort", requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }
}
