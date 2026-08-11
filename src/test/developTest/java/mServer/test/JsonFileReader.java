package mServer.test;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reads a json file
 */
public class JsonFileReader {
    
    private JsonFileReader() {}
    
    public static JsonObject readJson(String filePath) {
        try {
            URI u = JsonFileReader.class.getResource(filePath).toURI();
            Path path = Path.of(u);
            String jsonOutput = new String(Files.readAllBytes(path));
            return new Gson().fromJson(jsonOutput, JsonObject.class);        
        } catch(JsonSyntaxException | IOException | URISyntaxException ex) {
            fail("Exception reading jsonFile " + filePath + ": " + ex.getMessage());
        }
        return null;
    }
    
    public static JsonArray readJsonArray(String filePath) {
        try {
            URI u = JsonFileReader.class.getResource(filePath).toURI();
            Path path = Path.of(u);
            String jsonOutput = new String(Files.readAllBytes(path));
            return new Gson().fromJson(jsonOutput, JsonArray.class);        
        } catch(JsonSyntaxException | IOException | URISyntaxException ex) {
            fail("Exception reading jsonFile " + filePath + ": " + ex.getMessage());
        }
        return null;
    }
}
