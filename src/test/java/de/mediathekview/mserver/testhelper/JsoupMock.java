package de.mediathekview.mserver.testhelper;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/** */
public class JsoupMock {

  public static JsoupConnection mock(final String url, final String htmlFile) {
    return mockWithTextModifications(url, htmlFile, Function.identity());
  }

  public static JsoupConnection mockWithTextModifications(
      final String url, final String htmlFile, final Function<String, String> textModifier) {
    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(url, htmlFile);
    return mockWithTextModifications(urlMapping, textModifier);
  }

  public static Document getFileDocument(final String htmlFile) throws IOException {
    return getFileDocumentWithModifications(htmlFile, Function.identity());
  }

  public static Document getFileDocumentWithModifications(
      final String htmlFile, final Function<String, String> textModifier) {
    final String fileContent = textModifier.apply(FileReader.readFile(htmlFile));
    return Jsoup.parse(fileContent);
  }

  public static JsoupConnection mock(final Map<String, String> urlMapping) {
    return mockWithTextModifications(urlMapping, Function.identity());
  }

  public static JsoupConnection mockWithTextModifications(
      final Map<String, String> urlMapping, final Function<String, String> textModifier) {
    final JsoupConnection connection = Mockito.mock(JsoupConnection.class);
    for (final Entry<String, String> urlMappingEntry : urlMapping.entrySet()) {
      final String url = urlMappingEntry.getKey();
      final String file = urlMappingEntry.getValue();
      try {
        final String fileContent = textModifier.apply(FileReader.readFile(file));
        final Document document = Jsoup.parse(fileContent);
        final Document XmlDocument = Jsoup.parse(fileContent, url, Parser.xmlParser());

        Mockito.when(connection.requestBodyAsString(url)).thenReturn(fileContent);
        Mockito.when(connection.requestBodyAsHtmlDocument(url)).thenReturn(document);
        Mockito.when(connection.requestBodyAsXmlDocument(org.mockito.Mockito.eq(url)))
            .thenReturn(XmlDocument);
      } catch (final IOException ioException) {
        LoggerFactory.getLogger(JsoupMock.class)
            .error("Something went wrong mocking the JSoupConnection for  {}", url, ioException);
      }
    }
    return connection;
  }
}
