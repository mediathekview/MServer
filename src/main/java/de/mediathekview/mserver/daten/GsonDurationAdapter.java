package de.mediathekview.mserver.daten;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

/**
 * GsonDurationAdapter
 * Serialize/Deserialize java.time.Duration to/from string
 *
 */
public class GsonDurationAdapter extends TypeAdapter<Duration> {
  
    @Override
    public void write(JsonWriter out, Duration duration) throws IOException {
        if (duration == null) {
            out.nullValue();
            return;
        }
        out.value(duration.toString());
    }

    @Override
    public Duration read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }
        return Duration.parse(in.nextString());
        
    }
}

