package online.omnia.mailparser.deserializers;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 07.07.2017.
 */
public class JsonTokenDeserializer implements JsonDeserializer<String>{
    @Override
    public String deserialize(JsonElement jsonElement, Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();
        if (status.equals("200")) {
            String token = object.get("data").getAsJsonObject().get("access_token").getAsString();
            System.out.println("Access token has been got successfully");
            return token;
        }
        System.out.println("Access token have been got unsuccessfully");
        System.out.println(message);
        return "";
    }
}
