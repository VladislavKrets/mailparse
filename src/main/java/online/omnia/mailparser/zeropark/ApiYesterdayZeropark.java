package online.omnia.mailparser.zeropark;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 11.10.2017.
 */
public class ApiYesterdayZeropark extends Thread {
    private AccountEntity accountsEntity;
    private CountDownLatch countDownLatch;

    public ApiYesterdayZeropark(AccountEntity accountsEntity, CountDownLatch countDownLatch) {
        this.accountsEntity = accountsEntity;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {

        try {
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(List.class, new JsonCampaignListDeserializer());
            Gson gson = builder.create();
            List<CampaignJson> campaignJsons = new ArrayList<>();
            String answer;
            if (accountsEntity.getApiKey() == null) return;
            SimpleDateFormat dateFormat;
            java.util.Date currentDate = new java.util.Date();
            long l = currentDate.getTime() - Main.deltaTime;
            for (long n = 0; n <= Main.days; n++) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                answer = HttpMethodUtils
                        .getMethod("https://panel.zeropark.com" + "/api/stats/campaign/all?interval=CUSTOM&startDate="
                                        + dateFormat.format(new java.util.Date(l))
                                        + "&endDate=" + dateFormat.format(new java.util.Date(l)),
                                accountsEntity.getApiKey());

                try {
                    campaignJsons = gson.fromJson(answer, List.class);
                } catch (Exception e) {
                    Utils.writeLog(e.toString());
                    return;
                }
                AbstractAdsetEntity abstractAdsetEntity;
                dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
                Map<String, String> parameters;
                AdsetEntity entity;
                AdsetEntity tempAdset;

                for (CampaignJson campaignJson : campaignJsons) {

                    abstractAdsetEntity = new AbstractAdsetEntity();
                    abstractAdsetEntity.setAccountId(accountsEntity.getAccountId());
                    abstractAdsetEntity.setCampaignId(campaignJson.getCampaignId());
                    abstractAdsetEntity.setConversions(campaignJson.getConversions());
                    abstractAdsetEntity.setSpent(campaignJson.getSpent());
                    abstractAdsetEntity.setCampaignName(campaignJson.getName());
                    try {
                        abstractAdsetEntity.setDate(new Date(dateFormat.parse(dateFormat.format(new Date(l))).getTime()));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    abstractAdsetEntity.setBuyerId(accountsEntity.getBuyerId());
                    abstractAdsetEntity.setReceiver("API");
                    parameters = Utils.getUrlParameters(campaignJson.getClickUrl());
                    if (parameters.containsKey("cab")) {
                        if (parameters.get("cab").matches("\\d+")
                                && MySQLAdsetDaoImpl.getInstance().getAffiliateByAfid(Integer.parseInt(parameters.get("cab"))) != null) {
                            abstractAdsetEntity.setAfid(Integer.parseInt(parameters.get("cab")));
                        } else {
                            abstractAdsetEntity.setAfid(0);
                        }
                    } else abstractAdsetEntity.setAfid(2);
                    if (abstractAdsetEntity.getSpent() != 0 || abstractAdsetEntity.getClicks() != 0 || abstractAdsetEntity.getImpressions() != 0) {
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
                }
                l = l - 24L * 60 * 60 * 1000;
            }
                countDownLatch.countDown();
            } catch(Exception e){
                e.printStackTrace();
            }
        }

}
