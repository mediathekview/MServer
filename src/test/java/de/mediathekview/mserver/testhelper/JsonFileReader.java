package de.mediathekview.mserver.testhelper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.fail;

/** Reads a json file */
public class JsonFileReader {

  private JsonFileReader() {}

  public static JsonObject readJson(final String filePath) {
    try {
      final URI u = JsonFileReader.class.getResource(filePath).toURI();
      final Path path = Paths.get(u);
      final String jsonOutput = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
      return new Gson().fromJson(jsonOutput, JsonObject.class);
    } catch (final JsonSyntaxException | IOException | URISyntaxException ex) {
      fail("Exception reading jsonFile " + filePath + ": " + ex.getMessage());
    }
    return null;
  }

  public static JsonArray readJsonArray(final String filePath) {
    try {
      final URI u = JsonFileReader.class.getResource(filePath).toURI();
      final Path path = Paths.get(u);
      final String jsonOutput = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
      return new Gson().fromJson(jsonOutput, JsonArray.class);
    } catch (final JsonSyntaxException | IOException | URISyntaxException ex) {
      fail("Exception reading jsonFile " + filePath + ": " + ex.getMessage());
    }
    return null;
  }
}
