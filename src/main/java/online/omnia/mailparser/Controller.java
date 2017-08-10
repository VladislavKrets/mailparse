package online.omnia.mailparser;

import online.omnia.mailparser.zoho.dao.MySQLDaoImpl;
import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;

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
            service.submit(new MailThread("orion_noreply@cmcm.com",
                    Utils.createPropertiesFile(accessEntity), accessEntity, countDownLatch));
        }
        try {
            countDownLatch.await();
            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
