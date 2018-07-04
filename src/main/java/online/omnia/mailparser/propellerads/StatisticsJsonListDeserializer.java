package online.omnia.mailparser.propellerads;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lollipop on 15.10.2017.
 */
public class StatisticsJsonListDeserializer implements JsonDeserializer<List<JsonStatistic>>{
    @Override
    public List<JsonStatistic> deserialize(JsonElement jsonElement, Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject().get("result").getAsJsonObject();
        List<JsonStatistic> jsonStatistics = new ArrayList<>();
        JsonElement element = object.get("rows");
        if (element != null) {
            JsonArray jsonArray = element.getAsJsonArray();
            JsonStatistic jsonStatistic;
            for (JsonElement arrayElement : jsonArray) {
                jsonStatistic  = new JsonStatistic();
                jsonStatistic.setClick(arrayElement.getAsJsonObject().get("click").getAsString());
                jsonStatistic.setConvers(arrayElement.getAsJsonObject().get("convers").getAsString());
                jsonStatistic.setConvrate(arrayElement.getAsJsonObject().get("convrate").getAsString());
                jsonStatistic.setCpm(arrayElement.getAsJsonObject().get("cpm").getAsString());
                jsonStatistic.setCtr(arrayElement.getAsJsonObject().get("ctr").getAsString());
                jsonStatistic.setProfit(arrayElement.getAsJsonObject().get("profit").getAsString());
                jsonStatistic.setShow(arrayElement.getAsJsonObject().get("show").getAsString());
                jsonStatistics.add(jsonStatistic);
            }
        }

        return jsonStatistics;
    }
}
