package online.omnia.mailparser.cheetah.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.daoentities.AdsetEntity;
import online.omnia.mailparser.daoentities.CheetahTokenEntity;
import online.omnia.mailparser.cheetah.deserializers.JsonAdsetListDeserializer;

import java.io.BufferedReader;
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
public class ApiNewTodayThread implements Runnable{
    private AccountEntity accountEntity;
    private CountDownLatch countDownLatch;

    public ApiNewTodayThread(AccountEntity accountEntity, CountDownLatch countDownLatch) {
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
                        + "\"start\":\"" + dateFormat.format(new Date(currentDate.getTime())) + "\","
                        + "\"end\":\"" + dateFormat.format(new Date(currentDate.getTime())) + "\"}";

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
                List<AdsetEntity> entityList = listGson.fromJson(lineBuilder.toString(), List.class);
                if (entityList == null){
                    countDownLatch.countDown();
                    return;
                }
                /*for (AdsetEntity adsetEntity : entityList) {
                    adsetEntity.setAccountId(accountEntity.getAccountId());
                    adsetEntity.setReceiver("API");
                    adsetEntity.setTime(new Time(System.currentTimeMillis() + 10800000L));
                    System.out.println(adsetEntity.getDate());
                    System.out.println(adsetEntity.getAdsetId());
                    if (MySQLAdsetDaoImpl.getInstance().isDateInTodayAdsets(adsetEntity.getDate(), adsetEntity.getAdsetId())) {
                        System.out.println("Updating adset");
                        MySQLAdsetDaoImpl.getInstance().updateTodayAdset(adsetEntity);
                    }
                    else{
                        System.out.println("Adding adset");
                        MySQLAdsetDaoImpl.getInstance().addTodayAdset(adsetEntity);
                    }
                    System.out.println("CR: " + adsetEntity.getCr());

                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }

        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());
    }
}
