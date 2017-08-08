package online.omnia.mailparser.zoho;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.AdEntity;
import online.omnia.mailparser.zoho.zohodeserializers.JsonZohoAccountDeserializer;
import online.omnia.mailparser.zoho.zohodeserializers.JsonZohoDataMessageDeserializer;
import online.omnia.mailparser.zoho.zohodeserializers.JsonZohoMessagesDeserializer;
import online.omnia.mailparser.zoho.zohoentities.ZohoAccount;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessage;
import online.omnia.mailparser.zoho.zohoentities.ZohoMessageData;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
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
 * Created by lollipop on 04.08.2017.
 */
public class ZohoMail {

    public List<AdEntity> parseMessage(String html) {
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
        List<AdEntity> adEntities = new ArrayList<>();
        String[] splitName;
        AdEntity adEntity;
        for (Element trElement : body) {
            trElements = trElement.select("td");
            adEntity = new AdEntity();
            if (headersList.contains("Ad Set")){
                adEntity.setName(trElements.get(headersList.indexOf("Ad Set")).text());
                if (!adEntity.getName().isEmpty()) {
                    splitName = adEntity.getName().split("\\(");
                    if (splitName != null && splitName.length ==2 ) adEntity.setNumber(splitName[1].replaceAll("\\)", ""));
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
    }
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
        props.put("mail.store.protocol", "pop3");
        return props;
    }

}
