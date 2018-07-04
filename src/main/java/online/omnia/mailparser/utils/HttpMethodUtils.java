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

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public static String getMethod(String urlPath, String token){
        System.out.println(urlPath);
        try {
            if (urlPath == null) return null;
            HttpGet httpGet = new HttpGet(urlPath);
            if (token != null) {
                if (token.contains("Bearer ")) httpGet.addHeader("Authorization", token);
                else httpGet.addHeader("api-token", token);
            }

            return getResponse(httpGet);
        } catch (IOException e) {
            System.out.println("Input output exception during executing get request:");
            e.printStackTrace();
        }
        return "";
    }

    public static String postMethod(String urlPath, List<NameValuePair> params, String token){
        try {
            if (urlPath == null || params == null) return null;
            HttpPost httpPost = new HttpPost(urlPath);
            httpPost.addHeader("api-token", token);
            httpPost.setEntity(new UrlEncodedFormEntity(params));

            return getResponse(httpPost);
        } catch (IOException e) {
            System.out.println("Input output exception during executing post request:");
            e.printStackTrace();
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
