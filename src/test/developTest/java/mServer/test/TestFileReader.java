package mServer.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class TestFileReader {
    private TestFileReader() {}
    
    public static String readFile(String filePath) {
        try {
            URI u = TestFileReader.class.getResource(filePath).toURI();
            Path path = Path.of(u);
            return new String(Files.readAllBytes(path));
        } catch(IOException | URISyntaxException ex) {
            fail("Exception reading file " + filePath + ": " + ex.getMessage());
        }
        return null;
    }
}
