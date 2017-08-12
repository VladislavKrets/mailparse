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
        Controller controller = new Controller();
        controller.emailCheetahNew();
    }
}
