package online.omnia.mailparser.cheetah.threads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.daoentities.AbstractAdsetEntity;
import online.omnia.mailparser.daoentities.CheetahTokenEntity;
import online.omnia.mailparser.cheetah.deserializers.JsonAdsetListDeserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 17.08.2017.
 */
public class ApiMonthThread implements Runnable{
    private AccountEntity accountEntity;
    private CountDownLatch countDownLatch;

    public ApiMonthThread(AccountEntity accountEntity, CountDownLatch countDownLatch) {
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
                HttpURLConnection httpcon;

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.registerTypeAdapter(List.class, new JsonAdsetListDeserializer(accessToken));
                Gson listGson = gsonBuilder.create();

                Date currentDate = new Date();
                System.out.println("Creating json request");
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.set(2017, Calendar.AUGUST, 1);
                Date firstDate = calendar.getTime();
                String str;
                String line;
                StringBuilder lineBuilder;
                byte[] outputBytes;
                OutputStream os;
                BufferedReader reader;
                List<AbstractAdsetEntity> entityList;
                Date date;
                for (long i = firstDate.getTime(); i <= currentDate.getTime(); i += 86400000L) {
                    httpcon = (HttpURLConnection) ((new URL("https://api.ori.cmcm.com/report/advertiser").openConnection()));
                    httpcon.setDoOutput(true);
                    httpcon.setRequestProperty("Content-Type", "application/json");
                    httpcon.setRequestProperty("Accept", "application/json,application/x.orion.v1+json");
                    httpcon.setRequestProperty("Authorization", "Bearer " + accessToken);
                    httpcon.setRequestMethod("POST");
                    httpcon.connect();
                    date = new Date(i);
                    System.out.println("Getting dates for " + date);

                    str = "{\"column\":[\"impression\",\"click\",\"conversion\",\"revenue\",\"cpc\",\"cpi\",\"cpm\",\"ctr\",\"cvr\",\"cpv\"],"
                            + "\"groupby\":[\"datetime\",\"adset\"],"
                            + "\"filter\":{},"
                            + "\"start\":\"" + dateFormat.format(date) + "\","
                            + "\"end\":\"" + dateFormat.format(date) + "\"}";
                    date = dateFormat.parse(dateFormat.format(date));
                    outputBytes = str.getBytes("UTF-8");
                    os = httpcon.getOutputStream();
                    os.write(outputBytes);
                    os.close();
                    reader = new BufferedReader(new InputStreamReader(httpcon.getInputStream()));

                    lineBuilder = new StringBuilder();
                    System.out.println("Getting answer");
                    while ((line = reader.readLine()) != null) {
                        lineBuilder.append(line);
                    }
                    reader.close();
                    System.out.println("Parsing answer");
                    entityList = listGson.fromJson(lineBuilder.toString(), List.class);
                    if (entityList == null) {
                        continue;

                    }
                    for (AbstractAdsetEntity abstractAdsetEntity : entityList) {
                        abstractAdsetEntity.setAccountId(accountEntity.getAccountId());
                        abstractAdsetEntity.setReceiver("API");
                        System.out.println(abstractAdsetEntity.getDate());
                        System.out.println(abstractAdsetEntity.getAdsetId());
                      /*  if (MySQLAdsetDaoImpl.getInstance().isDateInAdsets(abstractAdsetEntity.getDate(), abstractAdsetEntity.getAdsetId())) {
                            System.out.println("Updating adset");
                            MySQLAdsetDaoImpl.getInstance().updateAdset(abstractAdsetEntity);
                        } else {
                            System.out.println("Adding adset");
                            MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);
                        }*/
                    }
                }
                } catch(IOException | ParseException e){
                    e.printStackTrace();
                }


        countDownLatch.countDown();
        System.out.println(countDownLatch.getCount());
    }
}
