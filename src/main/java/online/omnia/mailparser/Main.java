package online.omnia.mailparser;

import online.omnia.mailparser.zoho.ZohoMail;
import online.omnia.mailparser.zoho.zohoentities.ZohoAccount;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessage;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessageData;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by lollipop on 04.08.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ZohoMail zohoMail = new ZohoMail();

       List<ZohoAccount> zohoAccounts = zohoMail.getZohoAccount();
       List<ZohoMessage> messages = zohoMail.getMessages(zohoAccounts.get(0).getAccountId());
       ZohoMessageData zohoMessageData = zohoMail.getZohoMessageData(zohoAccounts.get(0).getAccountId(), messages.get(0).getFolderId(), messages.get(0).getMessageId());
       Iterator<ZohoMessage> iterator = messages.iterator();
       ZohoMessage message;
       while (iterator.hasNext()) {
           message = iterator.next();
           if (!message.getSender().equals("orion_noreply@cmcm.com")) iterator.remove();
       }
       List<AdEntity> entities = zohoMail.parseMessage(zohoMessageData.getContent());
       for (AdEntity entity : entities) {
           System.out.println(entity.getName());
           System.out.println(entity.getCtr());
           System.out.println(entity.getDate());
           System.out.println(entity.getNumber());
           System.out.println(entity.getImpressions());
           System.out.println(entity.getSpent());
           System.out.println();
       }
    }
}
