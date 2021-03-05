package de.mediathekview.mserver.testhelper;

import static org.mockito.ArgumentMatchers.any;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.mockito.Mockito;


/** */
public class JsoupMock {
  
  public static JsoupConnection mock(String aUrl, String aHtmlFile) throws IOException {
    Map<String, String> aUrlMapping = new HashMap<String, String>();
    aUrlMapping.put(aUrl,  aHtmlFile);
    return mock(aUrlMapping);
  }

  public static Document getFileDocument(String url, String htmlFile) throws IOException {
    final String fileContent = FileReader.readFile(htmlFile);
    return Jsoup.parse(fileContent);
  }

  public static JsoupConnection mock(final Map<String, String> aUrlMapping) {
    final JsoupConnection connection = Mockito.mock(JsoupConnection.class);
    for (Entry<String, String> e : aUrlMapping.entrySet()) {
      String url = e.getKey();
      String file = e.getValue();
      try {
        final String fileContent = FileReader.readFile(file);
        final Document document = Jsoup.parse(fileContent);
        final Document XmlDocument = Jsoup.parse(fileContent, url, Parser.xmlParser());
        
        
        Mockito.when(connection.getString(url)).thenReturn(fileContent);
        Mockito.when(connection.getDocument(url)).thenReturn(document);
        Mockito.when(connection.getDocument(org.mockito.Mockito.eq(url), any(Parser.class))).thenReturn(XmlDocument);
      } catch (IOException error) {
        // TODO Auto-generated catch block
        error.printStackTrace();
      }
    }
    return connection;
    
  }
}
