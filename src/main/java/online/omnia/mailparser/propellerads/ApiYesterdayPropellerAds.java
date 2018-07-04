package online.omnia.mailparser.propellerads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import online.omnia.mailparser.Main;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AbstractAdsetEntity;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.daoentities.AdsetEntity;
import online.omnia.mailparser.utils.HttpMethodUtils;
import online.omnia.mailparser.utils.Utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 14.10.2017.
 */
public class ApiYesterdayPropellerAds extends Thread {
    private AccountEntity accountsEntity;
    private CountDownLatch countDownLatch;

    public ApiYesterdayPropellerAds(AccountEntity accountsEntity, CountDownLatch countDownLatch) {
        this.accountsEntity = accountsEntity;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(List.class, new StatisticsJsonListDeserializer());
            Gson gson = builder.create();
            String answer;
            List<JsonStatistic> jsonStatistics;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date currentDate = new java.util.Date();

            answer = HttpMethodUtils.getMethod("http://report.propellerads.com/?action=getStats&key="
                    + accountsEntity.getApiKey() +
                    "&date_range=custom&date_start="
                    + dateFormat.format(new java.util.Date(currentDate.getTime() - Main.deltaTime - Main.days * 24L * 60 * 60 * 1000)) +
                    "date_end=" + dateFormat.format(new java.util.Date(currentDate.getTime() - Main.deltaTime))+
                    "&params[stat_columns][show,click,convers,convrate,cpm,ctr,profit]=show,click,convers,convrate,cpm,ctr,profit", null);
            System.out.println(answer);
            try {
                jsonStatistics = gson.fromJson(answer, List.class);
            } catch (Exception e) {
                Utils.writeLog(e.toString());
                return;
            }
            AbstractAdsetEntity abstractAdsetEntity;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
            Map<String, String> parameters;
            AdsetEntity entity;
            AdsetEntity tempAdset;
            for (JsonStatistic jsonStatistic : jsonStatistics) {
                if (isJsonStatisticsEmpty(jsonStatistic)) continue;
                abstractAdsetEntity = new AbstractAdsetEntity();
                abstractAdsetEntity.setAccountId(accountsEntity.getAccountId());
                abstractAdsetEntity.setClicks(Integer.parseInt(jsonStatistic.getShow()));
                abstractAdsetEntity.setConversions(Integer.parseInt(jsonStatistic.getConvers()));
                abstractAdsetEntity.setCpc(Double.parseDouble(jsonStatistic.getClick()));
                abstractAdsetEntity.setCpm(Double.parseDouble(jsonStatistic.getCpm()));
                abstractAdsetEntity.setCtr(Double.parseDouble(jsonStatistic.getCtr()));
                abstractAdsetEntity.setSpent(((int) (Double.parseDouble(jsonStatistic.getProfit()) * 100)) / 100.0);
                abstractAdsetEntity.setReceiver("API");
                abstractAdsetEntity.setBuyerId(accountsEntity.getBuyerId());
                try {
                    abstractAdsetEntity.setDate(new Date(simpleDateFormat.parse(simpleDateFormat.format(new Date(System.currentTimeMillis() - 86400000L))).getTime()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (Main.days != 0) {
                    if (MySQLAdsetDaoImpl.getInstance().isDateInAdsets(abstractAdsetEntity.getDate(), abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId())) {
                        MySQLAdsetDaoImpl.getInstance().updateAdset(abstractAdsetEntity);
                    } else MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);
                }
                else {
                    entity = MySQLAdsetDaoImpl.getInstance().isDateInTodayAdsets(abstractAdsetEntity.getDate(), abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId());
                    tempAdset = Utils.getAdset(abstractAdsetEntity);
                    if (entity != null){
                        tempAdset.setId(entity.getId());
                        MySQLAdsetDaoImpl.getInstance().updateTodayAdset(tempAdset);
                    } else MySQLAdsetDaoImpl.getInstance().addTodayAdset(tempAdset);

                }
            }
            countDownLatch.countDown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isJsonStatisticsEmpty(JsonStatistic jsonStatistic) {
        return jsonStatistic.getClick() == null && jsonStatistic.getConvers() == null
                && jsonStatistic.getConvrate() == null && jsonStatistic.getCpm() == null
                && jsonStatistic.getCtr() == null && jsonStatistic.getProfit() == null
                && jsonStatistic.getShow() == null;
    }

}
