package de.mediathekview.mserver.crawler.orf.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrfFilmDetailTaskJugendschutzTest extends OrfFilmDetailTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testJugendschutzNotAdded() throws IOException {
    setupHeadRequestForFileSize();
    final String requestUrl = "https://tvthek.orf.at/profile/Tatort/2713749/Tatort-Hetzjagd/14081980";
    final Connection connection = JsoupMock.mock(requestUrl, "/orf/orf_film_jugendschutz.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final Set<Film> actual = executeTask("Tatort", requestUrl, jsoupConnection);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }
}
