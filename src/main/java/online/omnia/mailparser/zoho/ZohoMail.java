package online.omnia.mailparser.zoho;

import javax.mail.*;
import java.io.*;
import java.util.*;

/**
 * Created by lollipop on 04.08.2017.
 */
public class ZohoMail {

    /*public List<AdsetEntity> parseMessage(String html) {
        Document doc = Jsoup.parse(html);
        Element table = doc.body().select("table").last();

        *//*Element table = doc.body().select("div")
                .first().select("div").first().select("blockquote")
                .select("div").first().select("div").first().select("div")
                .first().select("table").first();
        *//*
        Elements headers = table.select("thead").select("tr").last().select("th");
        List<String> headersList = new ArrayList<>();
        for (Element element : headers) {
            headersList.add(element.text());
        }
        Elements body = table.select("tbody").select("tr");
        Elements trElements;
        List<AdsetEntity> adEntities = new ArrayList<>();
        String[] splitName;
        AdsetEntity adEntity;
        for (Element trElement : body) {
            trElements = trElement.select("td");
            adEntity = new AdsetEntity();
            if (headersList.contains("Ad Set")){
                adEntity.setName(trElements.get(headersList.indexOf("Ad Set")).text());
                if (!adEntity.getName().isEmpty()) {
                    splitName = adEntity.getName().split("\\(");
                    if (splitName != null && splitName.length ==2 ) adEntity.setAdsetId(splitName[1].replaceAll("\\)", ""));
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
                adEntity.setCtr(trElements.get(headersList.indexOf("CTR")).text());
            }
            if (headersList.contains("Impressions")) {
                adEntity.setImpressions(trElements.get(headersList.indexOf("Impressions")).text());
            }
            if (headersList.contains("Spent")) {
                adEntity.setSpent(trElements.get(headersList.indexOf("Spent")).text());
            }
            adEntities.add(adEntity);
        }

        return adEntities;
    }*/
    //poppro.zoho.com
    //995
    //v.krets@omni-a.com
    //A0AzWhP9
    public static Properties getText(String serverAddress, String port,
                                 String userName, String password, String senderAddress) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.pop3.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.pop3.socketFactory.fallback", "false");
        props.put("mail.pop3.socketFactory.port", port);
        props.put("mail.pop3.port", port);
        props.put("mail.pop3.host", serverAddress);
        props.put("mail.pop3.user", userName);
        props.put("mail.store.protocol", "imap");
        return props;
    }

}
