package de.mediathekview.mlib.daten;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * GsonLocalDateTimeAdapter
 * Serialize/Deserialize java.time.LocalDateTime to string in format "yyyy-MM-dd'T'HH:mm:ss"
 *
 */

public class GsonLocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
  DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  
    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(localDateTime.format(formatter));
            
        }
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
          return LocalDateTime.parse(jsonReader.nextString(), formatter);
        } 
    }
}

