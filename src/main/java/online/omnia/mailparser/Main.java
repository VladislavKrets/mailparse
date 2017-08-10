package online.omnia.mailparser;

import online.omnia.mailparser.zoho.ZohoMail;
import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;

import javax.mail.*;
import java.io.*;
import java.util.Properties;

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
        MailThread mailThread = new MailThread("v.chugaev@omni-a.com", properties,
                emailAccessEntity);
        mailThread.run();

    }
}
