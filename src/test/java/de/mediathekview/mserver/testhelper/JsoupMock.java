package de.mediathekview.mserver.testhelper;

import java.io.IOException;
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
    Mockito.when(connection.get()).thenReturn(document);

    mockStatic(Jsoup.class);
        
    when(Jsoup.connect(aUrl)).thenReturn(connection);    
  }
}
