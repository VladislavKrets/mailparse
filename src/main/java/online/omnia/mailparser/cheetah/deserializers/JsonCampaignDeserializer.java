package online.omnia.mailparser.cheetah.deserializers;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 21.08.2017.
 */
public class JsonCampaignDeserializer implements JsonDeserializer<String>{
    @Override
    public String deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();
        System.out.println(status + " " + message);
        JsonElement element = object.get("data");
        if (element != null) {
            JsonObject data = element.getAsJsonObject();
            return data.get("name").getAsString();
        }
        return null;
    }
}
