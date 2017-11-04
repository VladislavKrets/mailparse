package online.omnia.mailparser.cheetah.deserializers;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 17.08.2017.
 */
public class JsonAdsetDeserializer implements JsonDeserializer<Adset>{
    @Override
    public Adset deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        System.out.println(jsonElement);
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();
        System.out.println(status + " " + message);
        JsonElement dataElement = object.get("data");
        if (dataElement != null) {
            return new Adset(
                    dataElement.getAsJsonObject().get("name").getAsString(),
                    dataElement.getAsJsonObject().get("campaign_id").getAsString()
            );

        }
        return null;
    }
}
