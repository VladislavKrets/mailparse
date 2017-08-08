package online.omnia.mailparser;

import javax.mail.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Month;
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

            Store store = session.getStore("pop3s");
            store.connect(serverAddress, userName, password);

            getMessages(store.getFolder("INBOX"));
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private void getMessages(Folder folderInbox) {
        try {
            folderInbox.open(Folder.READ_ONLY);
            javax.mail.Message[] messages;
            Address[] addresses;
            String[] splittedAddress;
            String address;
            BufferedReader reader;
            List<String> list = new ArrayList<>();
            int messagesCount = folderInbox.getMessageCount();
            int ost = messagesCount % 30;
            Date currentDate = new Date();
            for (int i = 0; i < messagesCount; i += 30) {

                List<AdEntity> adEntities;
                String messageId;
                if (messagesCount - 30 * (i + 1) <= ost)
                    messages = folderInbox.getMessages(messagesCount - ost + 1, messagesCount);
                else messages = folderInbox.getMessages(30 * i, 30 * (i + 1));
                for (javax.mail.Message message : messages) {
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

                        if (address.equals(senderAddress)) {
                            reader = new BufferedReader(new InputStreamReader(message.getInputStream()));
                            String line;
                            while (reader.ready()) {
                                line = reader.readLine();
                                if (line.endsWith("=")) {
                                    line = line.substring(0, line.length() - 1) + reader.readLine();
                                    if (line.startsWith("Date") || line.matches("\\d{8}.+")) list.add(line);
                                }
                            }
                            adEntities = parseMessage(list);

                            //ToDo
                        }
                    }
                }
            }

            folderInbox.close(false);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }

    }

    private List<AdEntity> parseMessage(List<String> parameters) throws IOException, MessagingException {

        String headersLine = parameters.get(0);

        List<String> headersList = new ArrayList<String>(Arrays.asList(headersLine.split("=09")));
        parameters.remove(0);

        List<AdEntity> adEntities = new ArrayList<>();
        String[] splitted;
        AdEntity adEntity;
        String[] splitName;
        for (String str : parameters) {
            splitted = str.split("=09");
            adEntity = new AdEntity();
            if (headersList.contains("Ad Set")) {
                adEntity.setName(splitted[headersList.indexOf("Ad Set")]);
                if (!adEntity.getName().isEmpty()) {
                    splitName = adEntity.getName().split("\\(");
                    if (splitName != null && splitName.length == 2)
                        adEntity.setNumber(splitName[1].replaceAll("\\)", ""));
                }
            }
            if (headersList.contains("Date")) {
                try {
                    adEntity.setDate(new SimpleDateFormat("yyyyMMdd").parse(splitted[headersList.indexOf("Date")]));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (headersList.contains("CTR")) {
                adEntity.setCtr(splitted[headersList.indexOf("CTR")]);
            }
            if (headersList.contains("Impressions")) {
                adEntity.setImpressions(splitted[headersList.indexOf("Impressions")]);
            }
            if (headersList.contains("Spent")) {
                adEntity.setSpent(splitted[headersList.indexOf("Spent")]);
            }
            adEntities.add(adEntity);
        }

        return adEntities;
    }
}
