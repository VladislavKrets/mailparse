package online.omnia.mailparser.zoho.dao;

import online.omnia.mailparser.Utils;
import online.omnia.mailparser.zoho.daoentities.AdsetEntity;
import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;
import online.omnia.mailparser.zoho.daoentities.EmailSuccessEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Map;

/**
 * Created by lollipop on 09.08.2017.
 */
public class MySQLDaoImpl implements MySQLDao{
    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static MySQLDaoImpl instance;

    static {
        configuration = new Configuration()
                .addAnnotatedClass(AdsetEntity.class)
                .addAnnotatedClass(EmailAccessEntity.class)
                .addAnnotatedClass(EmailSuccessEntity.class)
                .configure("/hibernate.cfg.xml");
        Map<String, String> properties = Utils.iniFileReader();
        configuration.setProperty("hibernate.connection.password", properties.get("password"));
        configuration.setProperty("hibernate.connection.username", properties.get("username"));
        String url = (properties.get("url")
                .startsWith("jdbc:mysql://") ? properties.get("url") : "jdbc:mysql://" + properties.get("url")) +
                ":" + properties.get("port") + "/" + properties.get("dbname");
        configuration.setProperty("hibernate.connection.url", url);
        Utils.setLogPath(properties.get("log"));
        while (true) {
            try {
                sessionFactory = configuration.buildSessionFactory();
                break;
            } catch (PersistenceException e) {
                try {
                    System.out.println("Can't connect to db");
                    System.out.println("Waiting for 30 seconds");
                    Thread.sleep(30000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    @Override
    public List<EmailAccessEntity> getMailsByCheck(int check) {
        Session session = sessionFactory.openSession();
        List<EmailAccessEntity> emails = session.createQuery("from EmailAccessEntity where check_cheetah=:check", EmailAccessEntity.class)
                .setParameter("check", check).getResultList();
        session.close();
        return emails;
    }

    @Override
    public EmailSuccessEntity getEmailSuccessByMessageId(String messageId) {
        Session session = sessionFactory.openSession();
        EmailSuccessEntity emailSuccessEntity = session.createQuery("from EmailSuccessEntity where message_id=:messageId", EmailSuccessEntity.class)
                .setParameter("messageId", messageId).getSingleResult();
        session.close();
        return emailSuccessEntity;
    }

    @Override
    public synchronized void addNewEmailSuccess(EmailSuccessEntity emailSuccessEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(emailSuccessEntity);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public List<EmailSuccessEntity> getEmailSuccess(int firstSuccessValue, int secondSuccessValue) {
        Session session = sessionFactory.openSession();
        List<EmailSuccessEntity> emailSuccessEntities = session
                .createQuery("from EmailSuccessEntity where success>:firstValue and success<:secondValue", EmailSuccessEntity.class)
                .setParameter("firstValue", firstSuccessValue)
                .setParameter("secondValue", secondSuccessValue)
                .getResultList();
        session.close();
        return emailSuccessEntities;
    }

    @Override
    public synchronized void updateSuccessEntity(EmailSuccessEntity emailSuccessEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.createQuery("update EmailSuccessEntity set success=:success where message_id=:messageId")
                .setParameter("success", emailSuccessEntity.getSuccess())
                .setParameter("messageId", emailSuccessEntity.getMessageId());
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public synchronized void addAdset(AdsetEntity adsetEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(adsetEntity);
        session.getTransaction().commit();
        session.close();
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static synchronized MySQLDaoImpl getInstance() {
        if (instance == null) instance = new MySQLDaoImpl();
        return instance;
    }
}
