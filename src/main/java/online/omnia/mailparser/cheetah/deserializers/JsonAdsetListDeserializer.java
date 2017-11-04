package online.omnia.mailparser.cheetah.deserializers;

import com.google.gson.*;
import online.omnia.mailparser.daoentities.AbstractAdsetEntity;
import online.omnia.mailparser.utils.HttpMethodUtils;
import online.omnia.mailparser.utils.Utils;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by lollipop on 17.08.2017.
 */
public class JsonAdsetListDeserializer implements JsonDeserializer<List<AbstractAdsetEntity>>{

    private String token;

    public JsonAdsetListDeserializer(String token) {
        this.token = token;
    }

    @Override
    public List<AbstractAdsetEntity> deserialize(JsonElement jsonElement,
                                                 Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        String status = object.get("status").getAsString();
        String message = object.get("message").getAsString();

        System.out.println(status + " " + message);
        List<String> titles = new ArrayList<>();

        JsonElement data = object.get("data");
        if (data.isJsonArray()) {
            System.out.println("Empty array");
            System.out.println("Returned: " + jsonElement);
            return null;
        }

        JsonArray titleArray = data.getAsJsonObject().get("title").getAsJsonArray();
        for (JsonElement element : titleArray) {
            titles.add(element.getAsString());
        }
        List<AbstractAdsetEntity> entities = new ArrayList<>();
        JsonArray array = data.getAsJsonObject().get("data").getAsJsonArray();
        AbstractAdsetEntity adsetEntity;
        JsonArray arrayElement;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        for (JsonElement element : array) {
            arrayElement = element.getAsJsonArray();
            adsetEntity = new AbstractAdsetEntity();
            if (titles.contains("datetime")) {
                try {
                    adsetEntity.setDate(simpleDateFormat
                            .parse(arrayElement.get(titles.indexOf("datetime")).getAsString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (titles.contains("impression")) {
                adsetEntity.setImpressions(arrayElement.get(titles.indexOf("impression")).getAsInt());
            }
            if (titles.contains("ctr")) {
                adsetEntity.setCtr(arrayElement.get(titles.indexOf("ctr")).getAsDouble() * 100);
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

            }
            if (titles.contains("click")) {
                adsetEntity.setClicks(arrayElement.get(titles.indexOf("click")).getAsInt());
                if (adsetEntity.getClicks() != 0) adsetEntity.setCr(adsetEntity.getConversions() * 1.0 / adsetEntity.getClicks() * 100);

            }
            if (titles.contains("revenue")) {
                adsetEntity.setSpent(arrayElement.get(titles.indexOf("revenue")).getAsDouble());
            }
            if (titles.contains("adset")) {
                adsetEntity.setAdsetId(arrayElement.get(titles.indexOf("adset")).getAsString());
                String answer = HttpMethodUtils.getMethod("https://api.ori.cmcm.com/adset/" + adsetEntity.getAdsetId(), "Bearer " + token);
                GsonBuilder builder = new GsonBuilder();
                builder.registerTypeAdapter(Adset.class, new JsonAdsetDeserializer());
                builder.registerTypeAdapter(String.class, new JsonCampaignDeserializer());
                Gson gson = builder.create();
                Adset adset = gson.fromJson(answer, Adset.class);
                Map<String, String> parameters = Utils.getUrlParameters(adset.getClickUrl());
                if (parameters.containsKey("cab")) {
                    adsetEntity.setAfid(Integer.parseInt(parameters.get("cab")));
                }
                else adsetEntity.setAfid(2);
                adsetEntity.setAdsetName(adset.getAdsetName());
                adsetEntity.setCampaignId(adset.getCampaignId());
                answer = HttpMethodUtils.getMethod("https://api.ori.cmcm.com/campaign/" + adsetEntity.getCampaignId(), "Bearer " + token);
                adsetEntity.setCampaignName(gson.fromJson(answer, String.class));
                gson = null;
                builder = null;
            }
            entities.add(adsetEntity);
        }
        return entities;
    }
}
