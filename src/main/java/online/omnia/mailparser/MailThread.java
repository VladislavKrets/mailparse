package online.omnia.mailparser;

import online.omnia.mailparser.zoho.dao.MySQLDaoImpl;
import online.omnia.mailparser.zoho.daoentities.AdsetEntity;
import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;
import online.omnia.mailparser.zoho.daoentities.EmailSuccessEntity;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lollipop on 08.08.2017.
 */
public class MailThread implements Runnable {
    private String senderAddress;
    private Properties props;
    private String userName;
    private String password;
    private String serverAddress;
    private EmailAccessEntity accessEntity;

    public MailThread(String senderAddress, Properties props,
                      EmailAccessEntity emailAccessEntity) {
        this.senderAddress = senderAddress;
        this.props = props;
        this.accessEntity = emailAccessEntity;
        this.userName = emailAccessEntity.getUsername();
        this.password = emailAccessEntity.getPassword();
        this.serverAddress = emailAccessEntity.getServerProtocol();
    }

    @Override
    public void run() {
        connect();
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
            store.connect(serverAddress, userName, password);

            getMessages(store.getFolder("INBOX"));
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private void getMessages(Folder folderInbox) {
        try {
            folderInbox.open(Folder.READ_WRITE);
            javax.mail.Message[] messages;
            List<String> list = new ArrayList<>();
            int messagesCount = folderInbox.getMessageCount();
            int ost = messagesCount % 30;
            Date currentDate = new Date();
            for (int i = 0; i < messagesCount; i += 30) {
                if (messagesCount - 30 * (i + 1) <= ost)
                    messages = folderInbox.getMessages(messagesCount - ost + 1, messagesCount);
                else messages = folderInbox.getMessages(30 * i, 30 * (i + 1));
                for (javax.mail.Message message : messages) {
                    checkMessage(list, currentDate, message);
                }
            }
            list = null;
            messages = null;
            folderInbox.close(false);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }

    }

    private boolean isMessageHandled(String messageId) {
        return MySQLDaoImpl.getInstance().getEmailSuccessByMessageId(messageId) != null;
    }

    private void checkMessage(List<String> list, Date currentDate, Message message) throws MessagingException, IOException {
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
                    MySQLDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                            messageId, 1, accessEntity.getId()
                    ));
                    Utils.writeLog(accessEntity.getUsername(), messageId, "ERROR PARSING");
                    return;
                }

                //ToDo

                for (AdsetEntity adsetEntity : adsetEntities) {
                    adsetEntity.setAccountId(accessEntity.getAccountId());
                    adsetEntity.setAccountName(accessEntity.getUsername());
                    MySQLDaoImpl.getInstance().addAdset(adsetEntity);
                }

                MySQLDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                        messageId, 0, accessEntity.getId()
                ));
                Utils.writeLog(accessEntity.getUsername(), messageId, "SUCCESS");

                addresses = null;
                splittedAddress = null;
                address = null;

            }
        }
    }

    public List<AdsetEntity> parseMessage(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.body().select("table").last();

        Elements headers = table.select("thead").select("tr").last().select("th");
        List<String> headersList = new ArrayList<>();
        for (Element element : headers) {
            headersList.add(element.text());
        }
        Elements body = table.select("tbody").select("tr");
        Elements trElements;
        List<AdsetEntity> adsetEntities = new ArrayList<>();
        String[] splitName;
        AdsetEntity adEntity;
        for (Element trElement : body) {
            trElements = trElement.select("td");
            adEntity = new AdsetEntity();
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

    private void buildAdset(List<String> headersList, Elements trElements, AdsetEntity adEntity) {
        String[] splitName;
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
                    adEntity.setDate(new SimpleDateFormat("yyyyMMdd").parse(trElements.get(headersList.indexOf("Date")).text()));
                } catch (ParseException e) {
                    e.printStackTrace();
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
            if (headersList.contains("Clicks")) {
                adEntity.setClicks(Integer.parseInt(trElements.get(headersList.indexOf("Clicks")).text()
                        .replaceAll(",", "")));
            }
            if (headersList.contains("Conversions")) {
                adEntity.setConversions(Integer.parseInt(trElements.get(headersList.indexOf("Conversions")).text()));
            }
            if (headersList.contains("CVR")) {
                adEntity.setCr(Double.parseDouble(trElements.get(headersList.indexOf("CVR")).text()
                        .replaceAll("%", "")));
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
        }
        catch (Exception e) {
            adEntity = null;
        }
    }

}
