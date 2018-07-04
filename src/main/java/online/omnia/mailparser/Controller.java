package online.omnia.mailparser;

import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;
import online.omnia.mailparser.cheetah.threads.*;
import online.omnia.mailparser.propellerads.ApiYesterdayPropellerAds;
import online.omnia.mailparser.utils.Utils;
import online.omnia.mailparser.zeropark.ApiYesterdayZeropark;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lollipop on 10.08.2017.
 */
public class Controller {
    public void zeroparkApiNew() {
        List<AccountEntity> accountEntities = MySQLAdsetDaoImpl.getInstance().getAccounts("Zeropark");
        accountEntities.removeIf(accountEntity -> !accountEntity.getStatisticsType().equals("API") && accountEntity.getActual() != 1);
        CountDownLatch countDownLatch = new CountDownLatch(accountEntities.size());

        ExecutorService service = Executors.newFixedThreadPool(10);
        for (AccountEntity accountEntity : accountEntities) {
            service.submit(new ApiYesterdayZeropark(accountEntity, countDownLatch));

        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void propelleradsApiNew() {
        List<AccountEntity> accountEntities = MySQLAdsetDaoImpl.getInstance().getAccounts("PropellerAds");
        accountEntities.removeIf(accountEntity -> !accountEntity.getStatisticsType().equals("API") || accountEntity.getActual() != 1);
        CountDownLatch countDownLatch = new CountDownLatch(accountEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (AccountEntity accountEntity : accountEntities) {
            service.submit(new ApiYesterdayPropellerAds(accountEntity, countDownLatch));
        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void todayApiNew() {
        List<AccountEntity> accountEntities = MySQLAdsetDaoImpl.getInstance().getAccounts("cheetah");
        accountEntities.removeIf(accountEntity -> !accountEntity.getStatisticsType().equals("API"));

        CountDownLatch countDownLatch = new CountDownLatch(accountEntities.size());

        ExecutorService service = Executors.newFixedThreadPool(10);
        for (AccountEntity accountEntity : accountEntities) {
            if (accountEntity.getStatisticsType().equals("API")) {
                service.submit(new ApiNewTodayThread(accountEntity, countDownLatch));
            }
        }
        try {
            countDownLatch.await();
            service.shutdown();
            MySQLAdsetDaoImpl.getSessionFactory().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void allApiNew() {
        List<AccountEntity> accountEntities = MySQLAdsetDaoImpl.getInstance().getAccounts("cheetah");
        accountEntities.removeIf(accountEntity -> !accountEntity.getStatisticsType().equals("API"));
        CountDownLatch countDownLatch = new CountDownLatch(accountEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (AccountEntity accountEntity : accountEntities) {
            if (accountEntity.getStatisticsType().equals("API")) {
                service.submit(new ApiMonthThread(accountEntity, countDownLatch));
            }
        }
        try {
            countDownLatch.await();
            service.shutdown();
            MySQLAdsetDaoImpl.getSessionFactory().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void cheetahNew() {
        System.out.println("Getting accounts for working with api cheetah");
        apiCheetahNew();
        System.out.println("Getting accounts for working with api propellerads");
        propelleradsApiNew();
        System.out.println("Getting accounts for working with api zeropark");
        zeroparkApiNew();
        System.out.println("Getting accounts for working with e-mail");
        emailCheetahNew();
    }

    public void apiCheetahNew() {
        List<AccountEntity> accountEntities = MySQLAdsetDaoImpl.getInstance().getAccounts("cheetah");
        System.out.println(accountEntities);
        accountEntities.removeIf(accountEntity -> accountEntity.getStatisticsType() == null || !accountEntity.getStatisticsType().equals("API") || accountEntity.getActual() != 1);

        CountDownLatch countDownLatch = new CountDownLatch(accountEntities.size());

        ExecutorService service = Executors.newFixedThreadPool(10);
        for (AccountEntity accountEntity : accountEntities) {
            service.submit(new ApiNewYesterdayThread(accountEntity, countDownLatch));

        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void emailCheetahNew() {
        List<EmailAccessEntity> accessEntities = MySQLAdsetDaoImpl.getInstance().getMailsByCheck(1, "E-MAIL");
        CountDownLatch countDownLatch = new CountDownLatch(accessEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (EmailAccessEntity accessEntity : accessEntities) {
            service.submit(new MailNewThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity, countDownLatch));
        }
        try {
            countDownLatch.await();
            service.shutdown();
            MySQLAdsetDaoImpl.getSessionFactory().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void emailCheetahCheck() {
        List<EmailSuccessEntity> emailSuccessEntities = MySQLAdsetDaoImpl
                .getInstance().getEmailSuccess(0, 5);
        CountDownLatch countDownLatch = new CountDownLatch(emailSuccessEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        EmailAccessEntity accessEntity;
        for (EmailSuccessEntity entity : emailSuccessEntities) {
            accessEntity = MySQLAdsetDaoImpl.getInstance().getAccessById(entity.getEmailAccessId());
            service.submit(new MailCheckThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity, countDownLatch, entity));
        }
        try {
            countDownLatch.await();
            service.shutdown();
            MySQLAdsetDaoImpl.getSessionFactory().close();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
