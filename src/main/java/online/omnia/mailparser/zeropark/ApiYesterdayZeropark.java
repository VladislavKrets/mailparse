package online.omnia.mailparser.zeropark;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AbstractAdsetEntity;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.utils.HttpMethodUtils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
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

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(List.class, new JsonCampaignListDeserializer());
        Gson gson = builder.create();
        List<CampaignJson> campaignJsons = new ArrayList<>();
        String answer;
        if (accountsEntity.getApiKey() == null) return;
        answer = HttpMethodUtils
                .getMethod("https://panel.zeropark.com" + "/api/stats/campaign/all?interval=YESTERDAY",
                        accountsEntity.getApiKey());

        campaignJsons = gson.fromJson(answer, List.class);
        AbstractAdsetEntity abstractAdsetEntity;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        for (CampaignJson campaignJson : campaignJsons) {

            abstractAdsetEntity = new AbstractAdsetEntity();
            abstractAdsetEntity.setAccountId(accountsEntity.getAccountId());
            abstractAdsetEntity.setCampaignId(campaignJson.getCampaignId());
            abstractAdsetEntity.setConversions(campaignJson.getConversions());
            abstractAdsetEntity.setSpent(campaignJson.getSpent());
            try {
                abstractAdsetEntity.setDate(dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis() - 86400000L))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            abstractAdsetEntity.setBuyerId(accountsEntity.getBuyerId());
            abstractAdsetEntity.setReceiver("API");
            if (MySQLAdsetDaoImpl.getInstance().isDateInAdsets(abstractAdsetEntity.getDate(), abstractAdsetEntity.getAccountId(), abstractAdsetEntity.getCampaignId())) {
                MySQLAdsetDaoImpl.getInstance().updateAdset(abstractAdsetEntity);
            }
            else MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);

        }
        countDownLatch.countDown();
    }
}
