package online.omnia.mailparser;

import online.omnia.mailparser.daoentities.EmailSuccessEntity;
import online.omnia.mailparser.threads.MailCheckThread;
import online.omnia.mailparser.threads.MailNewThread;
import online.omnia.mailparser.zoho.ZohoMail;
import online.omnia.mailparser.daoentities.EmailAccessEntity;

import javax.mail.*;
import java.io.*;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 07.08.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, MessagingException {
        ZohoMail mail = new ZohoMail();
        Properties properties = mail.getText("imappro.zoho.com", "993",
                "v.krets@omni-a.com", "A0AzWhP9", "v.chugaev@omni-a.com");

        EmailAccessEntity emailAccessEntity = new EmailAccessEntity();
        emailAccessEntity.setPassword("A0AzWhP9");
        emailAccessEntity.setUsername("v.krets@omni-a.com");
        emailAccessEntity.setServerPort("993");
        emailAccessEntity.setServerProtocol("imappro.zoho.com");
        /*MailNewThread mailThread = new MailNewThread("v.chugaev@omni-a.com", properties,
                emailAccessEntity, new CountDownLatch(1));
        mailThread.run();
*/
        MailCheckThread mailCheckThread = new MailCheckThread("v.chugaev@omni-a.com", properties,
                emailAccessEntity, new CountDownLatch(1), new EmailSuccessEntity("errr", 1, 2));
        mailCheckThread.run();
    }
}
