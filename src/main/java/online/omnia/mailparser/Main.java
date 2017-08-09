package online.omnia.mailparser;

import online.omnia.mailparser.zoho.ZohoMail;

import javax.mail.*;
import java.io.*;
import java.util.Properties;

/**
 * Created by lollipop on 07.08.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, MessagingException {
        ZohoMail mail = new ZohoMail();
        Properties properties = mail.getText("poppro.zoho.com", "995",
                "v.krets@omni-a.com", "A0AzWhP9", "v.chugaev@omni-a.com");
        MailThread mailThread = new MailThread("v.chugaev@omni-a.com", properties,
                "v.krets@omni-a.com", "A0AzWhP9", "poppro.zoho.com");
        mailThread.run();

    }
}
