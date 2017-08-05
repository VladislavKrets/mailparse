package online.omnia.mailparser.zoho.zohodeserializers;

import com.google.gson.*;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessage;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lollipop on 05.08.2017.
 */
public class JsonZohoMessagesDeserializer implements JsonDeserializer<List<ZohoMessage>>{
    @Override
    public List<ZohoMessage> deserialize(JsonElement jsonElement,
                                         Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonObject status = object.get("status").getAsJsonObject();
        String code = status.get("code").getAsString();
        String messageStatus = status.get("description").getAsString();
        System.out.println(code + " " + messageStatus);
        JsonElement messages = object.get("data");
        List<ZohoMessage> zohoMessages = new ArrayList<>();
        if (messages != null) {
            JsonArray messagesArray = messages.getAsJsonArray();
            JsonObject message;
            for (JsonElement element : messagesArray) {
                message = element.getAsJsonObject();
                if (element.toString().replaceAll("[{}]", "").isEmpty()) continue;
                zohoMessages.add(new ZohoMessage(
                        message.get("summary") == null ? null : message.get("summary").getAsString(),
                        new Date(message.get("sentDateInGMT").getAsLong()),
                        message.get("subject") == null ? null : message.get("subject").getAsString(),
                        message.get("messageId").getAsString(),
                        message.get("folderId").getAsString(),
                        message.get("sender").getAsString(),
                        new Date(message.get("receivedTime").getAsLong()),
                        message.get("fromAddress").getAsString()
                ));
            }
            return zohoMessages;
        }
        return null;
    }
}
