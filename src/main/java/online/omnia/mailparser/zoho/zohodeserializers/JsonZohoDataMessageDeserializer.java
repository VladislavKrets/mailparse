package online.omnia.mailparser.zoho.zohodeserializers;

import com.google.gson.*;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessage;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessageData;

import java.lang.reflect.Type;

/**
 * Created by lollipop on 05.08.2017.
 */
public class JsonZohoDataMessageDeserializer implements JsonDeserializer<ZohoMessageData>{

    @Override
    public ZohoMessageData deserialize(JsonElement jsonElement, Type type,
                                       JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonObject status = object.get("status").getAsJsonObject();
        String code = status.get("code").getAsString();
        String message = status.get("description").getAsString();
        System.out.println(code + " " + message);

        JsonElement messageData = object.get("data");
        if (messageData != null) {
            ZohoMessageData zohoMessageData = new ZohoMessageData(
                    messageData.getAsJsonObject().get("messageId").getAsString(),
                    messageData.getAsJsonObject().get("content").getAsString()
            );
            return zohoMessageData;
        }
        return null;
    }
}
