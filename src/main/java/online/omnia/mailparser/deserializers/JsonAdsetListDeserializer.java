package online.omnia.mailparser.deserializers;

import com.google.gson.*;
import online.omnia.mailparser.utils.HttpMethodUtils;
import online.omnia.mailparser.daoentities.AdsetEntity;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 17.08.2017.
 */
public class JsonAdsetListDeserializer implements JsonDeserializer<List<AdsetEntity>>{

    private String token;

    public JsonAdsetListDeserializer(String token) {
        this.token = token;
    }

    @Override
    public List<AdsetEntity> deserialize(JsonElement jsonElement,
                                         Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();

        System.out.println(status + " " + message);
        List<String> titles = new ArrayList<>();
        JsonObject data = object.get("data").getAsJsonObject();
        JsonArray titleArray = data.get("title").getAsJsonArray();
        for (JsonElement element : titleArray) {
            titles.add(element.getAsString());
        }
        List<AdsetEntity> entities = new ArrayList<>();
        JsonArray array = data.get("data").getAsJsonArray();
        AdsetEntity adsetEntity;
        JsonArray arrayElement;
        for (JsonElement element : array) {
            arrayElement = element.getAsJsonArray();
            adsetEntity = new AdsetEntity();
            if (titles.contains("datetime")) {
                try {
                    adsetEntity.setDate(new SimpleDateFormat("yyyyMMdd")
                            .parse(arrayElement.get(titles.indexOf("datetime")).getAsString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (titles.contains("impression")) {
                adsetEntity.setImpressions(arrayElement.get(titles.indexOf("impression")).getAsInt());
            }
            if (titles.contains("ctr")) {
                adsetEntity.setCtr(arrayElement.get(titles.indexOf("ctr")).getAsDouble());
            }
            if (titles.contains("cpm")) {
                adsetEntity.setCpm(arrayElement.get(titles.indexOf("cpm")).getAsDouble());
            }
            if (titles.contains("cpi")) {
                adsetEntity.setCpi(arrayElement.get(titles.indexOf("cpi")).getAsDouble());
            }
            if (titles.contains("conversion")) {
                adsetEntity.setConversions(arrayElement.get(titles.indexOf("conversion")).getAsInt());
            }
            if (titles.contains("cpc")) {
                adsetEntity.setCpc(arrayElement.get(titles.indexOf("cpc")).getAsDouble());
                if (adsetEntity.getCpc() != 0) adsetEntity.setCr((adsetEntity.getConversions() / adsetEntity.getCpc()) * 100);

            }
            if (titles.contains("click")) {
                adsetEntity.setClicks(arrayElement.get(titles.indexOf("click")).getAsInt());
            }
            if (titles.contains("revenue")) {
                adsetEntity.setSpent(arrayElement.get(titles.indexOf("revenue")).getAsDouble());
            }
            if (titles.contains("adset")) {
                adsetEntity.setAdsetId(arrayElement.get(titles.indexOf("adset")).getAsString());
                String answer = HttpMethodUtils.getMethod("adset/" + adsetEntity.getAdsetId(), token);
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(String.class, new JsonAdsetDeserializer());
                Gson gson = builder.create();
                adsetEntity.setAdsetName(gson.fromJson(answer, String.class));
                gson = null;
                builder = null;
            }
            entities.add(adsetEntity);
        }
        return entities;
    }
}
