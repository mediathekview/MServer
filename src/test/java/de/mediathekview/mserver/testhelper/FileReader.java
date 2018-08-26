package de.mediathekview.mserver.testhelper;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReader {

  private FileReader() {
  }

  public static String readFile(String filePath) {
    try {
      Path path = getPath(filePath);
      return new String(Files.readAllBytes(path), "UTF-8");
    } catch (IOException ex) {
      fail("Exception reading file " + filePath + ": " + ex.getMessage());
    }
    return null;
  }

  public static Path getPath(String filePath) {

    if (!filePath.startsWith("/")) {
      filePath = "/" + filePath;
    }

    URI u = null;
    try {
      u = FileReader.class.getResource(filePath).toURI();
    } catch (URISyntaxException ex) {
      fail("Exception reading file " + filePath + ": " + ex.getMessage());
    }
    return Paths.get(u);
  }
}
