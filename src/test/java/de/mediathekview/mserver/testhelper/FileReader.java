package de.mediathekview.mserver.testhelper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

public class FileReader {

  private FileReader() {}

  public static String readFile(final String filePath) {
    return readFile(filePath, null);
  }

  public static String readFile(final String filePath, final String wireMockBaseUrl) {
    try {
      final Path path = getPath(filePath);
      final String readString = Files.readString(path);

      return wireMockBaseUrl == null
          ? readString
          : replaceAllWireMockUrls(readString, wireMockBaseUrl);
    } catch (final IOException ex) {
      fail("Exception reading file " + filePath + ": " + ex.getMessage());
    }
    return null;
  }

  private static String replaceAllWireMockUrls(
      final String readString, final String wireMockBaseUrl) {
    return readString.replaceAll("localhost:\\d+", wireMockBaseUrl);
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
