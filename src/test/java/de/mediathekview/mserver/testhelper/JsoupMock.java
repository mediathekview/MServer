package de.mediathekview.mserver.testhelper;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import static org.mockito.ArgumentMatchers.any;


/** */
public class JsoupMock {

  public static JsoupConnection mock(String aUrl, String aHtmlFile) throws IOException {
    Map<String, String> urlMapping = new HashMap<String, String>();
    urlMapping.put(aUrl,  aHtmlFile);
    return mock(urlMapping);
  }

  public static Document getFileDocument(final String url, final String htmlFile)
      throws IOException {
        return getFileDocumentWithModifications(url,htmlFile,Function.identity());
  }

    public static Document getFileDocumentWithModifications(
            final String url, final String htmlFile, final Function<String, String> textModifier)
            throws IOException {
        final String fileContent = textModifier.apply(FileReader.readFile(htmlFile));
        return Jsoup.parse(fileContent);
    }

    public static JsoupConnection mock(final Map<String, String> urlMapping) {
      return mockWithTextModifications(urlMapping,Function.identity());
    }
    
  public static JsoupConnection mockWithTextModifications(final Map<String, String> urlMapping, Function<String,String> textModifier) {
    final JsoupConnection connection = Mockito.mock(JsoupConnection.class);
    for (Entry<String, String> urlMappingEntry : urlMapping.entrySet()) {
      String url = urlMappingEntry.getKey();
      String file = urlMappingEntry.getValue();
      try {
        final String fileContent = textModifier.apply(FileReader.readFile(file));
          final Document document = Jsoup.parse(fileContent);
        final Document XmlDocument = Jsoup.parse(fileContent, url, Parser.xmlParser());
        
        
        Mockito.when(connection.requestBodyAsString(url)).thenReturn(fileContent);
        Mockito.when(connection.requestBtmlDocument(url)).thenReturn(document);
        Mockito.when(connection.requestBodyAsXmlDocument(org.mockito.Mockito.eq(url))).thenReturn(XmlDocument);
      } catch (IOException ioException) {
          LoggerFactory.getLogger(JsoupMock.class).error("Something went wrong mocking the JSoupConnection for  {}",url,ioException);
          }

          }
    return connection;
  }
}
        o