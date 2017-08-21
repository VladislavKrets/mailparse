package online.omnia.mailparser.threads;

import online.omnia.mailparser.utils.Utils;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.AdsetEntity;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 10.08.2017.
 */
public class MailCheckThread extends MailNewThread{
    private EmailSuccessEntity emailSuccessEntity;

    public MailCheckThread(String senderAddress, Properties props,
                           EmailAccessEntity emailAccessEntity, CountDownLatch countDownLatch, EmailSuccessEntity emailSuccessEntity) {
        super(senderAddress, props, emailAccessEntity, countDownLatch);
        this.emailSuccessEntity = emailSuccessEntity;
    }

    @Override
    protected void checkMessage(List<String> list, Date currentDate, Message message) throws MessagingException, IOException {
        String messageId = "<No message id>";
        Address[] addresses;
        String[] splittedAddress;
        String address;
        List<AdsetEntity> adsetEntities;
        if (currentDate.getTime() - message.getSentDate().getTime() <= 2592000000L) {
            Enumeration<Header> headerEnumeration = message.getAllHeaders();
            while (headerEnumeration.hasMoreElements()) {
                Header header = headerEnumeration.nextElement();

                if (header.getName().startsWith("Message-Id")) {
                    messageId = header.getValue();
                    if (isMessageHandled(messageId)) return;
                }
                if (header.getName().equals("Subject")) {
                    if (!header.getValue().contains("Cheetah Ads Auto Report")) return;
                }
            }
            Utils.writeLog(userName, messageId, "");

            addresses = message.getFrom();

            splittedAddress = addresses[0].toString().split(" ");
            address = splittedAddress[splittedAddress.length - 1].replaceAll("[<>]", "");
            System.out.println(address);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String[] lines;
            StringBuilder html = new StringBuilder();
            boolean isHtml = false;
            if (address.equals(senderAddress)) {

                message.writeTo(outputStream);
                lines = outputStream.toString().replaceAll("\r", "").split("\n");

                for (int i = 0; i < lines.length; i++) {
                    if (lines[i] == null) break;

                    int length = lines[i].length();
                    if (lines[i].contains("text/html")) isHtml = true;
                    if (isHtml && lines[i].endsWith("=")) {

                        html.append(lines[i].substring(0, length - 1));
                    }
                }
                adsetEntities = parseMessage(html.toString());
                if (adsetEntities == null) {
                    emailSuccessEntity.setSuccess(emailSuccessEntity.getSuccess() + 1);
                    MySQLAdsetDaoImpl.getInstance().updateSuccessEntity(emailSuccessEntity);
                    Utils.writeLog(accessEntity.getUsername(), messageId, "REPEATED ERROR PARSING");
                    return;
                }

                //ToDo

                for (AdsetEntity adsetEntity : adsetEntities) {
                    adsetEntity.setAccountId(accessEntity.getAccountId());
                    MySQLAdsetDaoImpl.getInstance().addAdset(adsetEntity);
                }
                emailSuccessEntity.setSuccess(0);
                MySQLAdsetDaoImpl.getInstance().updateSuccessEntity(emailSuccessEntity);

                Utils.writeLog(accessEntity.getUsername(), messageId, "SUCCESS");

                addresses = null;
                splittedAddress = null;
                address = null;

            }
        }
    }
}
