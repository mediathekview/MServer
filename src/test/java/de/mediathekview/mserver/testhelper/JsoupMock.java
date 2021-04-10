package de.mediathekview.mserver.testhelper;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.ArgumentMatchers.any;

/** */
public class JsoupMock {

  public static Connection mock(final String url, final String htmlFile) throws IOException {
    return mockWikthTextModification(url, htmlFile, Function.identity());
  }

  public static Connection mockWikthTextModification(
      final String url, final String htmlFile, final Function<String, String> textModifier)
      throws IOException {
    final String fileContent = textModifier.apply(FileReader.readFile(htmlFile));
    final Document document = Jsoup.parse(fileContent);

    final Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
    Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
    Mockito.when(connection.get()).thenReturn(document);

    final JsoupConnection jsoupConnectionMock = Mockito.mock(JsoupConnection.class);

    Mockito.when(jsoupConnectionMock.getConnection(url)).thenReturn(connection);
    return jsoupConnectionMock.getConnection(url);
  }

  public static Document getFileDocument(final String url, final String htmlFile)
      throws IOException {
    final String fileContent = FileReader.readFile(htmlFile);
    return Jsoup.parse(fileContent);
  }

  public static Document getFileDocumentWithModifications(
      final String url, final String htmlFile, final Function<String, String> textModifier)
      throws IOException {
    final String fileContent = textModifier.apply(FileReader.readFile(htmlFile));
    return Jsoup.parse(fileContent);
  }

  public static Document mockXml(final String aUrl, final String aXmlFile) throws IOException {
    final Connection connection = mock(aUrl, aXmlFile);

    Mockito.when(connection.parser(any())).thenReturn(connection);

    return getFileDocument(aUrl, aXmlFile);
  }

  public static Map<String, Connection> mock(final Map<String, String> aUrlMapping) {
    final Map<String, Connection> resultMap = new HashMap<>();

    aUrlMapping.forEach(
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
