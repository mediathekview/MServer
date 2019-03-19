package de.mediathekview.mserver.testhelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class FileReader {

  private FileReader() {}

  public static String readFile(final String filePath) {
    try {
      final Path path = getPath(filePath);
      return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    } catch (final IOException ex) {
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
    } catch (final URISyntaxException ex) {
      fail("Exception reading file " + filePath + ": " + ex.getMessage());
    }
    return Paths.get(u);
  }
}
