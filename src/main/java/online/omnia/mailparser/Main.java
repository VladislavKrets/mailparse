package online.omnia.mailparser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.deserializers.JsonTokenDeserializer;
import online.omnia.mailparser.threads.ApiNewThread;

import javax.mail.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lollipop on 07.08.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, MessagingException {
        Controller controller = new Controller();
        controller.emailCheetahCheck();

    }
}
