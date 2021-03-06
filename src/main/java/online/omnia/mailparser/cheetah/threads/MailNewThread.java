package online.omnia.mailparser.cheetah.threads;

import online.omnia.mailparser.daoentities.AbstractAdsetEntity;
import online.omnia.mailparser.daoentities.AccountEntity;
import online.omnia.mailparser.utils.Utils;
import online.omnia.mailparser.dao.MySQLAdsetDaoImpl;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Decoder;

import javax.mail.*;
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 08.08.2017.
 */
public class MailNewThread implements Runnable {
    protected String senderAddress;
    protected Properties props;
    protected String userName;
    protected String password;
    protected String serverAddress;
    protected EmailAccessEntity accessEntity;
    protected CountDownLatch countDownLatch;
    private AccountEntity accountEntity;

    public MailNewThread(String senderAddress, Properties props,
                         EmailAccessEntity emailAccessEntity, CountDownLatch countDownLatch) {
        this.senderAddress = senderAddress;
        this.props = props;
        this.accessEntity = emailAccessEntity;
        this.userName = emailAccessEntity.getUsername();
        this.password = emailAccessEntity.getPassword();
        this.serverAddress = emailAccessEntity.getServerName().replaceAll(" ","");
        this.countDownLatch = countDownLatch;
        accountEntity = MySQLAdsetDaoImpl.getInstance().getAccount(accessEntity.getAccountId());

    }

    @Override
    public void run() {
        if (accountEntity.getActual() == 1) {
            connect();
        }
        countDownLatch.countDown();
    }

    private void connect() {
        try {
            Authenticator auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(userName, password);
                }
            };
            Session session = Session.getDefaultInstance(props, auth);

            Store store = session.getStore("imap");

            try {
                store.connect(serverAddress, userName, password);
            } catch (AuthenticationFailedException e) {
                e.printStackTrace();
                try {
                    Utils.writeLog(userName, "<No message Id>", e.getMessage());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                MySQLAdsetDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                        null, 1, accessEntity.getId()
                ));
                return;
            }
            System.out.println("getting messages");
            try {
                getMessages(store.getFolder("INBOX"));
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            MySQLAdsetDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                    null, 1, accessEntity.getId()
            ));

            try {
                Utils.writeLog(accessEntity.getUsername(), "<No message_id>", "ERROR PARSING");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("Exception during connecting to mail");
            e.printStackTrace();
        }
    }

    private void getMessages(Folder folderInbox) {
        try {

            folderInbox.open(Folder.READ_WRITE);
            javax.mail.Message[] messages = null;
            List<String> list = new ArrayList<>();
            int messagesCount = folderInbox.getMessageCount();
            int ost = messagesCount % 30;
            Date currentDate = new Date();
            for (int i = 0; i < messagesCount; i += 30) {
                if (messagesCount - 30 * (i + 1) <= ost) {
                    messages = folderInbox.getMessages(messagesCount - ost + 1, messagesCount);
                } else {
                    messages = folderInbox.getMessages(30 * i + 1, 30 * (i + 1));
                }

                for (javax.mail.Message message : messages) {
                    if (message.getSubject().contains("Cheetah Ads Auto Report")) {

                        checkMessage(list, currentDate, message);
                    }
                }
            }
            list = null;
            messages = null;
            folderInbox.close(false);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            System.out.println("error");
            MySQLAdsetDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                    null, 1, accessEntity.getId()
            ));
            try {
                Utils.writeLog(accessEntity.getUsername(), "<No message_id>", "ERROR PARSING");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        }

    }

    protected boolean isMessageHandled(String messageId) {
        return MySQLAdsetDaoImpl.getInstance().getEmailSuccessByMessageId(messageId) != null;
    }

    protected void checkMessage(List<String> list, Date currentDate, Message message) throws MessagingException, IOException {
        String messageId = "<No message id>";
        Address[] addresses;
        String[] splittedAddress;
        String address;
        List<AbstractAdsetEntity> adsetEntities = null;

        if (currentDate.getTime() - message.getSentDate().getTime() <= 2592000000L) {
            Enumeration<Header> headerEnumeration = message.getAllHeaders();
            BASE64Decoder base64Decoder = new BASE64Decoder();
            while (headerEnumeration.hasMoreElements()) {

                Header header = headerEnumeration.nextElement();

                if (header.getName().equalsIgnoreCase("Message-Id")) {
                    messageId = (org.apache.commons.codec.binary.Base64
                            .isBase64(header.getValue()) ? new String(base64Decoder.decodeBuffer(header.getValue()), "UTF-8") : header.getValue());

                        if (isMessageHandled(messageId)) {
                            System.out.println("message been handled");
                            return;
                        }
                }
            }
            System.out.println("Checked");
            Utils.writeLog(userName, messageId, "");

            addresses = message.getFrom();
            splittedAddress = addresses[0].toString().split(" ");
            address = splittedAddress[splittedAddress.length - 1].replaceAll("[<>]", "");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            String[] lines;
            StringBuilder html = new StringBuilder();
            boolean isHtml = false;
            String[] split;
            if (address.equals(senderAddress)) {
                message.writeTo(outputStream);

                lines = outputStream.toString().replaceAll("\r", "").split("\n");

                String line;
                for (int i = 0; i < lines.length; i++) {
                    if (lines[i] == null) break;
                    line = org.apache.commons.codec.binary.Base64.isBase64(lines[i])
                            ? new String(base64Decoder.decodeBuffer(lines[i])) : lines[i];

                    int length = line.length();
                    if (line.contains("text/html") || (line.contains("<") && line.contains(">"))) isHtml = true;
                    if (isHtml && line.endsWith("=")) {
                        html.append(line.substring(0, length - 1));
                    }
                    else if (isHtml) {
                        html.append(line);
                    }
                }
                    adsetEntities = parseMessage(html.toString());

                System.out.println("Message parsed");
                if (adsetEntities == null) {
                    MySQLAdsetDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                            messageId, 1, accessEntity.getId()
                    ));
                    Utils.writeLog(accessEntity.getUsername(), messageId, "ERROR PARSING");
                    return;
                }

                for (AbstractAdsetEntity abstractAdsetEntity : adsetEntities) {
                    abstractAdsetEntity.setAccountId(accessEntity.getAccountId());
                    abstractAdsetEntity.setBuyerId(accountEntity.getBuyerId());
                    MySQLAdsetDaoImpl.getInstance().addAdset(abstractAdsetEntity);
                }

                MySQLAdsetDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                        messageId, 0, accessEntity.getId()
                ));
                Utils.writeLog(accessEntity.getUsername(), messageId, "SUCCESS");

                addresses = null;
                splittedAddress = null;
                address = null;

            }
        }
    }

    public List<AbstractAdsetEntity> parseMessage(String html) {
        Document doc = Jsoup.parse(html.replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
        Element table = doc.body().select("table").last();

        Elements headers = table.select("thead").select("tr").last().select("th");
        List<String> headersList = new ArrayList<>();
        for (Element element : headers) {
            headersList.add(element.text());
        }
        Elements body = table.select("tbody").select("tr");
        Elements trElements;
        List<AbstractAdsetEntity> adsetEntities = new ArrayList<>();
        String[] splitName;
        AbstractAdsetEntity adEntity;
        for (Element trElement : body) {
            trElements = trElement.select("td");
            adEntity = new AbstractAdsetEntity();
            buildAdset(headersList, trElements, adEntity);
            if (adEntity == null) {
                return null;
            }
            adsetEntities.add(adEntity);
        }

        doc = null;
        table = null;
        headers = null;
        body = null;
        headers = null;
        return adsetEntities;
    }

    private void buildAdset(List<String> headersList, Elements trElements, AbstractAdsetEntity adEntity) {
        String[] splitName;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTF"));
        try {
            if (headersList.contains("Ad Set")) {
                adEntity.setAdsetName(trElements.get(headersList.indexOf("Ad Set")).text());
                if (!adEntity.getAdsetName().isEmpty()) {
                    splitName = adEntity.getAdsetName().split("\\(");
                    if (splitName != null && splitName.length == 2)
                        adEntity.setAdsetId(splitName[1].replaceAll("\\)", ""));
                }
            }
            if (headersList.contains("Date")) {
                try {
                    adEntity.setDate(new java.sql.Date(simpleDateFormat.parse(trElements.get(headersList.indexOf("Date")).text()).getTime()));
                    //adEntity.setTime(new Time(adEntity.getDate().getTime()));

                } catch (ParseException e) {
                    e.printStackTrace();
                    System.out.println("Exception during parsing date");
                    return;
                }
            }
            if (headersList.contains("Campaign")) {
                adEntity.setCampaignName(trElements.get(headersList.indexOf("Campaign")).text());
                if (!adEntity.getCampaignName().isEmpty()) {
                    splitName = adEntity.getCampaignName().split("\\(");
                    if (splitName != null && splitName.length == 2)
                        adEntity.setCampaignId(splitName[1].replaceAll("\\)", ""));
                }
            }
            if (headersList.contains("CTR")) {
                adEntity.setCtr(Double.parseDouble(trElements.get(headersList.indexOf("CTR"))
                        .text().replaceAll("%", "")));
            }
            if (headersList.contains("Impressions")) {
                adEntity.setImpressions(Integer.parseInt(trElements.get(headersList.indexOf("Impressions"))
                        .text().replaceAll(",", "")));
            }
            if (headersList.contains("Spent")) {

                adEntity.setSpent(Double.parseDouble(trElements.get(headersList.indexOf("Spent"))
                        .text().replaceAll("\\$", "")));
            }
            if (headersList.contains("Conversions")) {
                adEntity.setConversions(Integer.parseInt(trElements.get(headersList.indexOf("Conversions")).text()));
            }

            if (headersList.contains("Clicks")) {
                adEntity.setClicks(Integer.parseInt(trElements.get(headersList.indexOf("Clicks")).text()
                        .replaceAll(",", "")));
                if (adEntity.getClicks() != 0) adEntity.setCr(adEntity.getConversions() * 1.0 / adEntity.getClicks() * 100);

            }

            if (headersList.contains("CPM")) {
                adEntity.setCpm(Double.parseDouble(trElements.get(headersList.indexOf("CPM")).text()
                        .replaceAll("\\$", "")));
            }
            if (headersList.contains("CPC")) {
                adEntity.setCpc(Double.parseDouble(trElements.get(headersList.indexOf("CPC")).text()
                        .replaceAll("\\$", "")));
               }
            if (headersList.contains("CPI")) {
                adEntity.setCpi(Double.parseDouble(trElements.get(headersList.indexOf("CPI")).text()
                        .replaceAll("\\$", "")));
            }
            adEntity.setReceiver("E-MAIL");

        } catch (Exception e) {
            adEntity = null;
            System.out.println("Exception during parsing mail");
            e.printStackTrace();
        }
    }

}
