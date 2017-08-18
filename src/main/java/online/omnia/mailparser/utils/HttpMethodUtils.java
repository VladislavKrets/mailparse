package online.omnia.mailparser.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpMethodUtils {
    private static String baseUrl;
    private static CloseableHttpClient httpClient;

    static {
        baseUrl = "https://api.ori.cmcm.com/";
        httpClient = HttpClients.createDefault();
    }
    public HttpMethodUtils() {

    }
    public static String getToken(String clientId, String clientCredentials){
        try {
            HttpPost httpPost = new HttpPost(baseUrl + "oauth/access_token");
            List<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new BasicNameValuePair("grant_type", "client_credentials"));
            nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
            nameValuePairs.add(new BasicNameValuePair("client_secret", clientCredentials));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            return getResponse(httpPost);
        } catch (IOException e) {
            System.out.println("Input output exception during getting access token:");
            System.out.println(e.getMessage());
        }
        return "";
    }
    public static String getMethod(String urlPath, String token){
        try {
            if (urlPath == null) return null;
            HttpGet httpGet = new HttpGet(baseUrl + urlPath);
            httpGet.addHeader("Authorization", "Bearer " + token);
            return getResponse(httpGet);
        } catch (IOException e) {
            System.out.println("Input output exception during executing get request:");
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static String postMethod(String urlPath, List<NameValuePair> params, String token){
        try {
            if (urlPath == null || params == null) return null;
            HttpPost httpPost = new HttpPost(baseUrl + urlPath);
            httpPost.addHeader("Authorization", "Bearer " + token);
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return getResponse(httpPost);
        } catch (IOException e) {
            System.out.println("Input output exception during executing post request:");
            System.out.println(e.getMessage());
        }
        return "";
    }

    private static String getResponse(HttpUriRequest httpRequest) throws IOException {
        CloseableHttpResponse response = httpClient.execute(httpRequest);
        StringBuilder serverAnswer = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
        String answer;
        while ((answer = reader.readLine()) != null) {
            serverAnswer.append(answer);
        }
        reader.close();
        response.close();
        return serverAnswer.toString();
    }


}
