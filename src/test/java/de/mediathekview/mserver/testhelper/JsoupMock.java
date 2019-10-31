package de.mediathekview.mserver.testhelper;

import static org.mockito.ArgumentMatchers.any;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;

/** */
public class JsoupMock {
  
  public static Connection mock(String aUrl, String aHtmlFile) throws IOException {
    final String fileContent = FileReader.readFile(aHtmlFile);
    final Document document = Jsoup.parse(fileContent);

    final Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
    Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
    Mockito.when(connection.get()).thenReturn(document);

    final JsoupConnection jsoupConnectionMock = Mockito.mock(JsoupConnection.class);

    Mockito.when(jsoupConnectionMock.getConnection(aUrl)).thenReturn(connection);
    return jsoupConnectionMock.getConnection(aUrl);
  }

  public static Document getFileDocument(String url, String htmlFile) throws IOException {
    final String fileContent = FileReader.readFile(htmlFile);
    return Jsoup.parse(fileContent);
  }

  public static Document mockXml(String aUrl, String aXmlFile) throws IOException {
    Connection connection = mock(aUrl, aXmlFile);

    Mockito.when(connection.parser(any())).thenReturn(connection);

    return getFileDocument(aUrl, aXmlFile);
  }

  public static Map<String, Connection> mock(final Map<String, String> aUrlMapping) {
    final Map<String, Connection> resultMap = new HashMap<>();

    aUrlMapping
        .forEach(
            (url, resultFileName) -> {
              final String fileContent = FileReader.readFile(resultFileName);
              final Document document = Jsoup.parse(fileContent);

              final Connection connection = Mockito.mock(Connection.class);
              try {
                Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
                Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
                Mockito.when(connection.get()).thenReturn(document);

                final JsoupConnection jsoupConnectionMock = Mockito.mock(JsoupConnection.class);

                Mockito.when(jsoupConnectionMock.getConnection(url)).thenReturn(connection);

              } catch (final IOException ex) {
                Logger.getLogger(JsoupMock.class.getName()).log(Level.SEVERE, null, ex);
              }

              resultMap.put(url, connection);

            });

    return resultMap;

  }
}
