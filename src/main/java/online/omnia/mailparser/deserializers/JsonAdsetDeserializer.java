package online.omnia.mailparser.deserializers;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 17.08.2017.
 */
public class JsonAdsetDeserializer implements JsonDeserializer<String>{
    @Override
    public String deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();
        System.out.println(status + " " + message);
        JsonElement dataElement = object.get("data");
        if (dataElement != null) {
            String name = dataElement.getAsJsonObject().get("name").getAsString();
            return name;
        }
        return null;
    }
}
