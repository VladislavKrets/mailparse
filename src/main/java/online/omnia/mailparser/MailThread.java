package online.omnia.mailparser;

import online.omnia.mailparser.zoho.daoentities.AdsetEntity;
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

    public MailThread(String senderAddress, Properties props,
                      String username, String password, String serverAddress) {
        this.senderAddress = senderAddress;
        this.props = props;
        this.userName = username;
        this.password = password;
        this.serverAddress = serverAddress;
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

    private void checkMessage(List<String> list, Date currentDate, Message message) throws MessagingException, IOException {
        String messageId;
        Address[] addresses;
        String[] splittedAddress;
        String address;
        BufferedReader reader;
        List<AdsetEntity> adEntities;
        if (currentDate.getTime() - message.getSentDate().getTime() <= 2592000000L) {
            Enumeration<Header> headerEnumeration = message.getAllHeaders();
            while (headerEnumeration.hasMoreElements()) {
                Header header = headerEnumeration.nextElement();
                if (header.getName().startsWith("Message-Id")) {
                    messageId = header.getValue();
                }
            }
            addresses = message.getFrom();

            splittedAddress = addresses[0].toString().split(" ");
            address = splittedAddress[splittedAddress.length - 1].replaceAll("[<>]", "");
            System.out.println(address);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            String[] lines;
            String line = null;
            StringBuilder html = new StringBuilder();
            boolean isHtml = false;
            if (address.equals(senderAddress)) {

                message.writeTo(outputStream);
                lines = outputStream.toString().replaceAll("\r", "").split("\n");

                for (int i = 0; i < lines.length; i++) {
                    if (lines[i] == null) break;
                    if (lines[i].startsWith("Date")) {
                        int length = lines[i].length();
                        list.add(lines[i].substring(0, length - 1) + lines[i + 1]);
                    }
                    int length = lines[i].length();
                    if (lines[i].contains("text/html")) isHtml = true;
                    if (isHtml && lines[i].endsWith("=")) {

                        html.append(lines[i].substring(0, length - 1));

                    }
                }
                adEntities = parseMessage(html.toString());

                //ToDo
                for (AdsetEntity adsetEntity : adEntities) {
                    System.out.println(adsetEntity);
                }
                reader = null;
                addresses = null;
                splittedAddress = null;
                address = null;

            }
        }
    }

    public List<AdsetEntity> parseMessage(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.body().select("table").last();

        /*Element table = doc.body().select("div")
                .first().select("div").first().select("blockquote")
                .select("div").first().select("div").first().select("div")
                .first().select("table").first();
        */
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
            adsetEntities.add(adEntity);
            doc = null;
            table = null;
            headers = null;
            body = null;
        }
        return adsetEntities;
    }

    private List<AdsetEntity> parseMessage(List<String> parameters) throws IOException, MessagingException {

        String headersLine = parameters.get(0);

        List<String> headersList = new ArrayList<String>(Arrays.asList(headersLine.split("=09")));
        parameters.remove(0);

        List<AdsetEntity> adEntities = new ArrayList<>();
        String[] splitted;
        AdsetEntity adsetEntity;
        String[] splitName;
        for (String str : parameters) {
            splitted = str.split("=09");
            adsetEntity = new AdsetEntity();
            buildAdset(headersLine, headersList, splitted, adsetEntity);
            adEntities.add(adsetEntity);
        }
        headersLine = null;
        headersList = null;
        splitted = null;
        splitName = null;
        return adEntities;
    }

    private void buildAdset(String headersLine, List<String> headersList, String[] splitted, AdsetEntity adsetEntity) {
        String[] splitName;
        if (headersList.contains("Ad Set")) {
            adsetEntity.setAdsetName(splitted[headersList.indexOf("Ad Set")]);
            if (!adsetEntity.getAdsetName().isEmpty()) {
                splitName = adsetEntity.getAdsetName().split("\\(");
                if (splitName != null && splitName.length == 2)
                    adsetEntity.setAdsetId(splitName[1].replaceAll("\\)", ""));
            }
        }
        if (headersList.contains("Date")) {
            try {
                adsetEntity.setDate(new SimpleDateFormat("yyyyMMdd")
                        .parse(splitted[headersList.indexOf("Date")]));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (headersList.contains("CTR")) {
            adsetEntity.setCtr(Double.parseDouble(splitted[headersList
                    .indexOf("CTR")].replaceAll("%", "")));
        }
        if (headersList.contains("Impressions")) {
            adsetEntity.setImpressions(Integer.parseInt(splitted[headersList.indexOf("Impressions")]
                    .replaceAll(",", "")));
        }
        if (headersList.contains("Spent")) {
            adsetEntity.setSpent(Double.parseDouble(splitted[headersList
                    .indexOf("Spent")].replaceAll("\\$", "")));
        }
        if (headersList.contains("Clicks")) {
            adsetEntity.setClicks(Integer.parseInt(splitted[headersList.indexOf("Clicks")]
                    .replaceAll(",", "")));
        }
        if (headersList.contains("Conversions")) {
            adsetEntity.setConversions(Integer.parseInt(splitted[headersList.indexOf("Conversions")]));
        }
        if (headersList.contains("CVR")) {
            adsetEntity.setCr(Double.parseDouble(splitted[headersList.indexOf("CVR")]
                    .replaceAll("%", "")));
        }
        if (headersList.contains("CPM")) {
            adsetEntity.setCpm(Double.parseDouble(splitted[headersList.indexOf("CPM")]
                    .replaceAll("\\$", "")));
        }
        if (headersList.contains("CPC")) {
            adsetEntity.setCpc(Double.parseDouble(splitted[headersList.indexOf("CPC")]
                    .replaceAll("\\$", "")));
        }
        if (headersList.contains("CPI")) {
            adsetEntity.setCpi(Double.parseDouble(splitted[headersList.indexOf("CPI")]
                    .replaceAll("\\$", "")));
        }
        splitName = null;
    }
}
