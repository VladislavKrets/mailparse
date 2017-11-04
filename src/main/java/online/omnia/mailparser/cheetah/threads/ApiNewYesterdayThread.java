package online.omnia.mailparser.cheetah.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.daoentities.*;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.cheetah.deserializers.JsonAdsetListDeserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 17.08.2017.
 */
public class ApiNewYesterdayThread implements Runnable{
    private AccountEntity accountEntity;
    private CountDownLatch countDownLatch;

    public ApiNewYesterdayThread(AccountEntity accountEntity, CountDownLatch countDownLatch) {
        this.accountEntity = accountEntity;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        System.out.println("Getting token");
            CheetahTokenEntity tokenEntity = MySQLAdsetDaoImpl.getInstance().getToken(accountEntity.getAccountId());
            String accessToken = tokenEntity.getAccessToken();
            try {
                System.out.println("Connecting to cheetah");
                HttpURLConnection httpcon = (HttpURLConnection) ((new URL("https://api.ori.cmcm.com/report/advertiser").openConnection()));
                httpcon.setDoOutput(true);
                httpcon.setRequestProperty("Content-Type", "application/json");
                httpcon.setRequestProperty("Accept", "application/json,application/x.orion.v1+json");
                httpcon.setRequestProperty("Authorization", "Bearer " + accessToken);
                httpcon.setRequestMethod("POST");
                httpcon.connect();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date currentDate = new Date();
                System.out.println("Creating json request");
                String str = "{\"column\":[\"impression\",\"click\",\"conversion\",\"revenue\",\"cpc\",\"cpi\",\"cpm\",\"ctr\",\"cvr\",\"cpv\"],"
                        + "\"groupby\":[\"datetime\",\"adset\"],"
                        + "\"filter\":{},"
                        + "\"start\":\"" + dateFormat.format(new Date(currentDate.getTime() - 86400000L)) + "\","
                        + "\"end\":\"" + dateFormat.format(new Date(currentDate.getTime() - 86400000L)) + "\"}";

                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = httpcon.getOutputStream();
                os.write(outputBytes);
                os.close();
                BufferedReader reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));
                String line;
                StringBuilder lineBuilder = new StringBuilder();
                System.out.println("Getting answer");
                while ((line = reader.readLine()) != null) {
                    lineBuilder.append(line);
                }
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(List.class, new JsonAdsetListDeserializer(accessToken));
                Gson listGson = gsonBuilder.create();
                System.out.println("Parsing answer");
                List<AbstractAdsetEntity> entityList = listGson.fromJson(lineBuilder.toString(), List.class);
                if (entityList == null){
                    countDownLatch.countDown();
                    System.out.println(countDownLatch);
                    return;
                }
                for (AbstractAdsetEntity abstractAdsetEntity : entityList) {
                    abstractAdsetEntity.setAccountId(accountEntity.getAccountId());
                    abstractAdsetEntity.setReceiver("API");
                    abstractAdsetEntity.setBuyerId(accountEntity.getBuyerId());


                    System.out.println(abstractAdsetEntity.getDate());
                    System.out.println(abstractAdsetEntity.getAdsetId());
                    if (MySQLAdsetDaoImpl.getInstance().isDateInAdsets(abstractAdsetEntity.getDate(), abstractAdsetEntity.getAdsetId(), abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId())) {
                        System.out.println("Updating adset");
                        MySQLAdsetDaoImpl.getInstance().updateAdset(abstractAdsetEntity);
                    }
                    else{
                        System.out.println("Adding adset");
                        try {
                            MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("CR: " + abstractAdsetEntity.getCr());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());
    }
}
