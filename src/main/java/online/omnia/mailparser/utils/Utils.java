package online.omnia.mailparser.utils;

import online.omnia.mailparser.daoentities.EmailAccessEntity;

import java.io.*;
import java.sql.Date;
import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by lollipop on 09.08.2017.
 */
public class Utils {
    private static FileWriter logWriter;
    private static String path;
    public static synchronized Map<String, String> iniFileReader() {
        Map<String, String> properties = new HashMap<>();
        try(BufferedReader iniFileReader = new BufferedReader(new FileReader("email_cheetah.ini"))) {
            String property;
            String[] propertyArray;
            while ((property = iniFileReader.readLine()) != null) {
                propertyArray = property.split("=");
                if (property.contains("=")) {
                    properties.put(propertyArray[0], propertyArray[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static Properties createPropertiesFile(EmailAccessEntity emailAccessEntity) {
        Properties props = new Properties();
        if (emailAccessEntity.getSslOn() == 1) {
            props.put("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        props.put("mail.imap.socketFactory.fallback", "false");
        props.put("mail.imap.socketFactory.port", emailAccessEntity.getServerPort());
        props.put("mail.imap.port", emailAccessEntity.getServerPort());
        props.put("mail.imap.host", emailAccessEntity.getServerName().replaceAll(" ", ""));
        props.put("mail.imap.user", emailAccessEntity.getUsername());
        props.put("mail.store.protocol", "imap");
        //props.put("mail.debug", "true");
        return props;
    }

    public static void setLogPath(String path) {
        Utils.path = path;
    }
    public static synchronized void writeLog(String userName, String mailId, String result) throws IOException {
        if (logWriter == null) {
            if (path == null) {
                File file = new File("email_cheetah.log");
                if (!file.exists()) file.createNewFile();
                logWriter = new FileWriter(file, true);
            }
            else {
                File file;
                System.out.println(path);
                String tempPath = path.replaceAll("\\\\", "/");

                if (path.contains("/")) {
                    String dirPath = tempPath.substring(0, tempPath.lastIndexOf("/"));
                    file = new File(dirPath);
                    if (!file.exists()){
                        file.mkdirs();
                        file = new File(path);
                        file.createNewFile();
                    }
                }
                else {
                    file = new File(path);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                }
                System.out.println(file.getPath());
                logWriter = new FileWriter(new File(path), true);
            }
            Date date = new Date(System.currentTimeMillis());
            Time time = new Time(System.currentTimeMillis());
            logWriter.write(String.format("%s %s %s %s %s%n", date.toString(),
                    time.toString(), userName, mailId, result));
            logWriter.flush();
        }

    }
}
