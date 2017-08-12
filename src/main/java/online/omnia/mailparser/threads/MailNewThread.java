package online.omnia.mailparser.threads;

import online.omnia.mailparser.Utils;
import online.omnia.mailparser.dao.MySQLDaoImpl;
import online.omnia.mailparser.daoentities.AdsetEntity;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;
import org.apache.commons.codec.binary.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.misc.BASE64Decoder;

import javax.mail.*;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
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

    public MailNewThread(String senderAddress, Properties props,
                         EmailAccessEntity emailAccessEntity, CountDownLatch countDownLatch) {
        this.senderAddress = senderAddress;
        this.props = props;
        this.accessEntity = emailAccessEntity;
        this.userName = emailAccessEntity.getUsername();
        this.password = emailAccessEntity.getPassword();
        this.serverAddress = emailAccessEntity.getServerProtocol();
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        connect();
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

            store.connect(serverAddress, userName, password);
            System.out.println("getting messages");
            getMessages(store.getFolder("INBOX"));
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
            MySQLDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                    null, 1, accessEntity.getId()
            ));

            try {
                Utils.writeLog(accessEntity.getUsername(), "<No message_id>", "ERROR PARSING");
            } catch (IOException e1) {
                e1.printStackTrace();
            }

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

            System.out.println("error");
            MySQLDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
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
        return MySQLDaoImpl.getInstance().getEmailSuccessByMessageId(messageId) != null;
    }

    protected void checkMessage(List<String> list, Date currentDate, Message message) throws MessagingException, IOException {
        String messageId = "<No message id>";
        Address[] addresses;
        String[] splittedAddress;
        String address;
        List<AdsetEntity> adsetEntities = null;

        if (currentDate.getTime() - message.getSentDate().getTime() <= 2592000000L) {
            Enumeration<Header> headerEnumeration = message.getAllHeaders();
            while (headerEnumeration.hasMoreElements()) {
                Header header = headerEnumeration.nextElement();
                if (header.getName().startsWith("Message-Id")) {
                    messageId = header.getValue();

                    try {
                        if (isMessageHandled(messageId)) {
                            System.out.println("message been handled");
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
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
                BASE64Decoder base64Decoder = new BASE64Decoder();

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
                    MySQLDaoImpl.getInstance().addNewEmailSuccess(new EmailSuccessEntity(
                            messageId, 1, accessEntity.getId()
                    ));
                    Utils.writeLog(accessEntity.getUsername(), messageId, "ERROR PARSING");
                    return;
                }

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
        Document doc = Jsoup.parse(html.replaceAll("&lt;", "<").replaceAll("&gt;", ">"));
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
        } catch (Exception e) {
            adEntity = null;
        }
    }

}
