package online.omnia.mailparser.cheetah.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import online.omnia.mailparser.Main;
import online.omnia.mailparser.daoentities.*;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.cheetah.deserializers.JsonAdsetListDeserializer;
import online.omnia.mailparser.utils.Utils;

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
public class ApiNewYesterdayThread implements Runnable {
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
                    + "\"start\":\"" + dateFormat.format(new Date(currentDate.getTime() - Main.deltaTime - Main.days * 24L * 60 * 60 * 1000)) + "\","
                    + "\"end\":\"" + dateFormat.format(new Date(currentDate.getTime() - Main.deltaTime)) + "\"}";

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
            System.out.println(lineBuilder.toString());
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.registerTypeAdapter(List.class, new JsonAdsetListDeserializer(accessToken));
            Gson listGson = gsonBuilder.create();
            System.out.println("Parsing answer");
            List<AbstractAdsetEntity> entityList = null;
            try {
                entityList = listGson.fromJson(lineBuilder.toString(), List.class);
            } catch (Exception e) {
                Utils.writeLog(e.toString());
                return;
            }
            if (entityList == null) {
                countDownLatch.countDown();
                System.out.println(countDownLatch);
                return;
            }
            AbstractAdsetEntity adsetEntity;
            AdsetEntity entity;
            AdsetEntity tempAdset;
            for (AbstractAdsetEntity abstractAdsetEntity : entityList) {
                abstractAdsetEntity.setAccountId(accountEntity.getAccountId());
                abstractAdsetEntity.setReceiver("API");
                abstractAdsetEntity.setBuyerId(accountEntity.getBuyerId());


                System.out.println(abstractAdsetEntity.getDate());
                System.out.println(abstractAdsetEntity.getAdsetId());
                if (Main.days != 0) {
                    adsetEntity = MySQLAdsetDaoImpl.getInstance().isDateInAdsets(abstractAdsetEntity.getDate(),
                            abstractAdsetEntity.getAdsetId(), abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId());

                    if (adsetEntity != null) {
                        System.out.println("Updating adset");
                        MySQLAdsetDaoImpl.getInstance().updateAdset(abstractAdsetEntity);
                        adsetEntity = null;
                    } else {
                        System.out.println(abstractAdsetEntity);
                        System.out.println("Adding adset");
                        try {
                            MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                else {
                    entity = MySQLAdsetDaoImpl.getInstance()
                            .isDateInTodayAdsets(abstractAdsetEntity.getDate(),
                                    abstractAdsetEntity.getAdsetId(),
                                    abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId());
                    System.out.println(entity);
                    tempAdset = Utils.getAdset(abstractAdsetEntity);
                    if (entity != null){
                        tempAdset.setId(entity.getId());
                        MySQLAdsetDaoImpl.getInstance().updateTodayAdset(tempAdset);
                    } else MySQLAdsetDaoImpl.getInstance().addTodayAdset(tempAdset);

                }
                System.out.println("CR: " + abstractAdsetEntity.getCr());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());
    }
}
