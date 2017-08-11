package online.omnia.mailparser;

import online.omnia.mailparser.dao.MySQLDaoImpl;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;
import online.omnia.mailparser.threads.MailCheckThread;
import online.omnia.mailparser.threads.MailNewThread;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by lollipop on 10.08.2017.
 */
public class Controller {
    public void emailCheetahNew() {
        List<EmailAccessEntity> accessEntities = MySQLDaoImpl.getInstance().getMailsByCheck(1);
        CountDownLatch countDownLatch = new CountDownLatch(accessEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        for (EmailAccessEntity accessEntity : accessEntities) {
            service.submit(new MailNewThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity, countDownLatch));
        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void emailCheetahCheck() {
        List<EmailSuccessEntity> emailSuccessEntities =  MySQLDaoImpl
                .getInstance().getEmailSuccess(0, 5);
        CountDownLatch countDownLatch = new CountDownLatch(emailSuccessEntities.size());
        ExecutorService service = Executors.newFixedThreadPool(10);
        EmailAccessEntity accessEntity;
        for (EmailSuccessEntity entity : emailSuccessEntities) {
            accessEntity = MySQLDaoImpl.getInstance().getAccessById(entity.getEmailAccessId());
            service.submit(new MailCheckThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity, countDownLatch, entity));
        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
