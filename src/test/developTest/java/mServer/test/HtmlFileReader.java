package mServer.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.Assert.fail;

public class HtmlFileReader {
    private HtmlFileReader() {}
    
    public static String readHtmlPage(String filePath) {
        try {
            URI u = HtmlFileReader.class.getResource(filePath).toURI();
            Path path = Paths.get(u);
            return new String(Files.readAllBytes(path));
        } catch(IOException | URISyntaxException ex) {
            fail("Exception reading htmlFile " + filePath + ": " + ex.getMessage());
        }
        return null;
    }
}
