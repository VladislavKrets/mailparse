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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

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
                System.out.println(trElements.get(headersList.indexOf("Spent")).text());
                adEntity.setSpent(trElements.get(headersList.indexOf("Spent")).text());
            }
            adEntities.add(adEntity);
        }

        return adEntities;
    }

    public ZohoMessageData getZohoMessageData(String accountId, String folderId, String messageId) throws IOException {
        String answer = getResponse(String.format("accounts/%s/folders/%s/messages/%s/content",
                accountId, folderId, messageId));
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(ZohoMessageData.class, new JsonZohoDataMessageDeserializer());
        Gson gson = builder.create();
        ZohoMessageData zohoMessageData = gson.fromJson(answer, ZohoMessageData.class);
        return zohoMessageData;
    }

    public List<ZohoAccount> getZohoAccount() throws IOException {
        String answer = getResponse("accounts");
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(List.class, new JsonZohoAccountDeserializer());
        Gson gson = builder.create();
        List<ZohoAccount> accounts = gson.fromJson(answer, List.class);
        return accounts;
    }

    public List<ZohoMessage> getMessages(String accountId) throws IOException {
        String answer = getResponse(String.format("accounts/%s/messages/view", accountId));
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(List.class, new JsonZohoMessagesDeserializer());
        Gson gson = builder.create();
        List<ZohoMessage> messages = gson.fromJson(answer, List.class);
        return messages;
    }
    private String getResponse(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("https://mail.zoho.com/api/" + url);
        httpGet.addHeader("Authorization", "Zoho-authtoken 514d7668bae167c3eab9050e2fbd86e1");
        CloseableHttpResponse response = httpClient.execute(httpGet);
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        response.close();
        httpClient.close();
        return builder.toString();
    }
    public void getToken(String login, String password) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("https://accounts.zoho.eu/apiauthtoken/nb/create");
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new BasicNameValuePair("SCOPE", "ZohoMail/ZohoMailApi"));
        nameValuePairs.add(new BasicNameValuePair("EMAIL_ID", login));
        nameValuePairs.add(new BasicNameValuePair("PASSWORD", password));
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        CloseableHttpResponse response = httpClient.execute(httpPost);
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        response.close();
    }
}
