package online.omnia.mailparser.zoho.zohodeserializers;

import com.google.gson.*;
import online.omnia.mailparser.zoho.zohoentities.ZohoAccount;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 05.08.2017.
 */
public class JsonZohoAccountDeserializer implements JsonDeserializer<List<ZohoAccount>>{
    @Override
    public List<ZohoAccount> deserialize(JsonElement jsonElement, Type type,
                                   JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        JsonObject status = object.get("status").getAsJsonObject();
        String code = status.get("code").getAsString();
        String message = status.get("description").getAsString();
        System.out.println(code + " " + message);
        JsonElement accountElement = object.get("data");
        if (accountElement != null) {
            JsonArray array = accountElement.getAsJsonArray();
            List<ZohoAccount> accounts = new ArrayList<>();
            for (JsonElement element : array) {
                accounts.add(new ZohoAccount(
                        element.getAsJsonObject().get("country").getAsString(),
                        element.getAsJsonObject().get("accountName").getAsString(),
                        element.getAsJsonObject().get("displayName").getAsString(),
                        element.getAsJsonObject().get("primaryEmailAddress").getAsString(),
                        element.getAsJsonObject().get("accountId").getAsString()
                ));
            }

            return accounts;
        }
        return null;
    }
}
