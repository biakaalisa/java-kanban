package history;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeAdapter
        implements JsonSerializer<LocalDateTime>,
        JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(LocalDateTime src,
                                 Type type,
                                 JsonSerializationContext context) {

        return new JsonPrimitive(src.toString());
    }

    @Override
    public LocalDateTime deserialize(JsonElement json,
                                     Type type,
                                     JsonDeserializationContext context) {

        return LocalDateTime.parse(json.getAsString());
    }
}