package de.mediathekview.mserver.testhelper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/** */
public class JsoupMock {

  public static void mock(final String aUrl, final String aHtmlFile) throws IOException {
    final String fileContent = FileReader.readFile(aHtmlFile);
    final Document document = Jsoup.parse(fileContent);

    suppress(constructor(Jsoup.class));
    final Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
    Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
    Mockito.when(connection.get()).thenReturn(document);

    mockStatic(Jsoup.class);

    when(Jsoup.connect(aUrl)).thenReturn(connection);
  }

  public static void mock(final Map<String, String> aUrlMapping) {
    final Map<String, Connection> x = new HashMap<>();

    aUrlMapping
        .entrySet()
        .forEach(
            entry -> {
              final String fileContent = FileReader.readFile(entry.getValue());
              final Document document = Jsoup.parse(fileContent);

              suppress(constructor(Jsoup.class));
              final Connection connection = Mockito.mock(Connection.class);
              try {
                Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
                Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
                Mockito.when(connection.get()).thenReturn(document);
              } catch (final IOException ex) {
                Logger.getLogger(JsoupMock.class.getName()).log(Level.SEVERE, null, ex);
              }

              x.put(entry.getKey(), connection);
            });

    mockStatic(Jsoup.class);

    x.entrySet()
        .forEach(
            entry -> {
              final String url = entry.getKey();
              final Connection result = entry.getValue();

              when(Jsoup.connect(url)).thenReturn(result);
            });
  }
}
