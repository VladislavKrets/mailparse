package online.omnia.mailparser;

import online.omnia.mailparser.zoho.ZohoMail;
import online.omnia.mailparser.zoho.zohoentities.ZohoAccount;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessage;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessageData;

import java.io.IOException;
import java.util.List;

/**
 * Created by lollipop on 04.08.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        ZohoMail zohoMail = new ZohoMail();
       /* parser.connect("https://accounts.zoho.eu/login?servicename=VirtualOffice&hidesignup=true&serviceurl=http%3A%2F%2Fmail.zoho.eu&hide_secure=true&css=https://www.zoho.eu//css/prd-sign.css",
               "v.krets@omni-a.com", "A0AzWhP9");
        parser.getAdSets("https://mail.zoho.com/zm/popMail.do?accId=4078132000000008001&msgId=4078132000000013001&folId=4078132000000008013&att=0&entityId=4078132000000013001&entityType=1&feature=false");
    */
       List<ZohoAccount> zohoAccounts = zohoMail.getZohoAccount();
       List<ZohoMessage> messages = zohoMail.getMessages(zohoAccounts.get(0).getAccountId());
       ZohoMessageData zohoMessageData = zohoMail.getZohoMessageData(zohoAccounts.get(0).getAccountId(), messages.get(0).getFolderId(), messages.get(0).getMessageId());
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
