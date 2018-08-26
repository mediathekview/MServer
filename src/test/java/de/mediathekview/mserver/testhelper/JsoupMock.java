package de.mediathekview.mserver.testhelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockito.Mockito;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.api.support.membermodification.MemberMatcher.constructor;
import static org.powermock.api.support.membermodification.MemberModifier.suppress;

/**
 *
 */
public class JsoupMock {
  
  public static void mock(String aUrl, String aHtmlFile) throws IOException {
    String fileContent = FileReader.readFile(aHtmlFile);
    Document document = Jsoup.parse(fileContent);
    
    suppress(constructor(Jsoup.class));
    Connection connection = Mockito.mock(Connection.class);
    Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
    Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
    Mockito.when(connection.get()).thenReturn(document);

    mockStatic(Jsoup.class);
        
    when(Jsoup.connect(aUrl)).thenReturn(connection);    
  }
  
  public static void mock(Map<String, String> aUrlMapping) {
    Map<String, Connection> x = new HashMap<>();
    
    aUrlMapping.entrySet().forEach(entry -> {
      String fileContent = FileReader.readFile(entry.getValue());
      Document document = Jsoup.parse(fileContent);
    
      suppress(constructor(Jsoup.class));
      Connection connection = Mockito.mock(Connection.class);
      try {
        Mockito.when(connection.timeout(Mockito.anyInt())).thenReturn(connection);
        Mockito.when(connection.maxBodySize(0)).thenReturn(connection);
        Mockito.when(connection.get()).thenReturn(document);
      } catch (IOException ex) {
        Logger.getLogger(JsoupMock.class.getName()).log(Level.SEVERE, null, ex);
      }
      
      x.put(entry.getKey(), connection);
    });

    mockStatic(Jsoup.class);
        
    x.entrySet().forEach(entry -> {
      String url = entry.getKey();
      Connection result = entry.getValue();
      
      when(Jsoup.connect(url)).thenReturn(result);
    });            
  }
}
