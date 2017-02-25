package mServer.testhelper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Reads a json file
 */
public class JsonFileReader {
    public static JsonObject readJson(String filePath) throws IOException, URISyntaxException {
        URI u = JsonFileReader.class.getResource(filePath).toURI();
        Path path = Paths.get(u);
        String jsonOutput = new String(Files.readAllBytes(path));
        return new Gson().fromJson(jsonOutput, JsonObject.class);        
    }
}
