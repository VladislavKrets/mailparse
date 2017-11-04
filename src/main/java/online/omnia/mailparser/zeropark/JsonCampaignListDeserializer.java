package online.omnia.mailparser.zeropark;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 12.10.2017.
 */
public class JsonCampaignListDeserializer implements JsonDeserializer<List<CampaignJson>>{
    @Override
    public List<CampaignJson> deserialize(JsonElement jsonElement, Type type,
                            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        System.out.println(jsonElement);
        JsonObject object = jsonElement.getAsJsonObject();
        JsonArray elements = object.get("elements").getAsJsonArray();
        List<CampaignJson> campaignJsons = new ArrayList<>();
        CampaignJson campaignJson;
        for (JsonElement element : elements) {
            campaignJson = new CampaignJson();
            campaignJson.setCampaignId(element.getAsJsonObject().get("details").getAsJsonObject().get("id").getAsString());
            campaignJson.setName(element.getAsJsonObject().get("details").getAsJsonObject().get("name").getAsString());
            campaignJson.setSpent(element.getAsJsonObject().get("stats").getAsJsonObject().get("spent").getAsDouble());
            campaignJson.setConversions(element.getAsJsonObject().get("stats").getAsJsonObject().get("conversions").getAsInt());
            campaignJsons.add(campaignJson);
        }
        return campaignJsons;
    }
}
