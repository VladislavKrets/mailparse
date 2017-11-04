package online.omnia.mailparser.dao;

import online.omnia.mailparser.daoentities.*;
import online.omnia.mailparser.utils.Utils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.NoResultException;
import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lollipop on 09.08.2017.
 */
public class MySQLAdsetDaoImpl implements MySQLDao{
    private static Configuration configuration;
    private static SessionFactory sessionFactory;
    private static MySQLAdsetDaoImpl instance;

    static {
        configuration = new Configuration()
                .addAnnotatedClass(AbstractAdsetEntity.class)
                .addAnnotatedClass(EmailAccessEntity.class)
                .addAnnotatedClass(EmailSuccessEntity.class)
                .addAnnotatedClass(CheetahTokenEntity.class)
                .addAnnotatedClass(AccountEntity.class)
                //.addAnnotatedClass(AdsetEntity.class)
                .configure("/hibernate_adset.cfg.xml");
        Map<String, String> properties = Utils.iniFileReader();
        configuration.setProperty("hibernate.connection.password", properties.get("password"));
        configuration.setProperty("hibernate.connection.username", properties.get("username"));
        String jdbcURL = (properties.get("url")
                .startsWith("jdbc:mysql://") ? properties.get("url") : "jdbc:mysql://" + properties.get("url"));
        String url = (!jdbcURL.endsWith("/") ? jdbcURL + "/" : jdbcURL) + properties.get("dbname");
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
    public AccountEntity getAccount(int id) {
        Session session = sessionFactory.openSession();
        AccountEntity accountEntity = session.createQuery("from AccountEntity where account_id=:id", AccountEntity.class)
                .setParameter("id", id)
                .getSingleResult();
        session.close();
        return accountEntity;
    }
    @Override
    public List<EmailAccessEntity> getMailsByCheck(int check, String statisticType) {
        Session session = sessionFactory.openSession();
        List<EmailAccessEntity> emails = session.createQuery("from EmailAccessEntity where check_cheetah=:check", EmailAccessEntity.class)
                .setParameter("check", check)
                .getResultList();
        session.close();
        return emails;
    }

    @Override
    public EmailSuccessEntity getEmailSuccessByMessageId(String messageId) {
        Session session = sessionFactory.openSession();
        EmailSuccessEntity emailSuccessEntity = null;
        try {
            emailSuccessEntity = session.createQuery("from EmailSuccessEntity where message_id=:messageId", EmailSuccessEntity.class)
                    .setParameter("messageId", messageId).getSingleResult();
        } catch (NoResultException e) {
            emailSuccessEntity = null;
        }
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
    public synchronized void addAdset(AbstractAdsetEntity abstractAdsetEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(abstractAdsetEntity);
        session.getTransaction().commit();
        session.close();
    }
    public synchronized void addTodayAdset(AdsetEntity adsetEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.save(adsetEntity);
        session.getTransaction().commit();
        session.close();
    }
    @Override
    public EmailAccessEntity getAccessById(int id) {
        Session session = sessionFactory.openSession();
        EmailAccessEntity accessEntity = null;
        try {
            accessEntity = session.createQuery("from EmailAccessEntity where id=:id", EmailAccessEntity.class)
                    .setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            accessEntity = null;
        }
        session.close();
        return accessEntity;
    }

    public synchronized void updateAdset(AbstractAdsetEntity abstractAdsetEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        session.createQuery("update AbstractAdsetEntity set CTR=:ctr, date=:date, impressions=:impressions, spent=:spent, clicks=:clicks, CR=:cr, CPC=:cpc, CPM=:cpm, conversions=:conversions, CPI=:cpi where adset_id=:adsetId and account_id=:accountId")
                .setParameter("ctr", abstractAdsetEntity.getCtr())
                .setParameter("date", abstractAdsetEntity.getDate())
                .setParameter("impressions", abstractAdsetEntity.getImpressions())
                .setParameter("spent", abstractAdsetEntity.getSpent())
                .setParameter("clicks", abstractAdsetEntity.getClicks())
                .setParameter("cr", abstractAdsetEntity.getCr())
                .setParameter("cpc", abstractAdsetEntity.getCpc())
                .setParameter("cpm", abstractAdsetEntity.getCpm())
                .setParameter("conversions", abstractAdsetEntity.getConversions())
                .setParameter("cpi", abstractAdsetEntity.getCpi())
                .setParameter("adsetId", abstractAdsetEntity.getAdsetId())
                .setParameter("accountId", abstractAdsetEntity.getAccountId())
                .executeUpdate();
        session.getTransaction().commit();
        session.close();
    }
    public synchronized void updateTodayAdset(AdsetEntity adsetEntity) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();/*
        session.createQuery("update AdsetEntity set CTR=:ctr, date=:date, impressions=:impressions, spent=:spent, clicks=:clicks, CR=:cr, CPC=:cpc, CPM=:cpm, conversions=:conversions, CPI=:cpi, time=:time where adset_id=:adsetId and account_id=:accountId")
                .setParameter("ctr", adsetEntity.getCtr())
                .setParameter("date", adsetEntity.getDate())
                .setParameter("impressions", adsetEntity.getImpressions())
                .setParameter("spent", adsetEntity.getSpent())
                .setParameter("clicks", adsetEntity.getClicks())
                .setParameter("cr", adsetEntity.getCr())
                .setParameter("cpc", adsetEntity.getCpc())
                .setParameter("cpm", adsetEntity.getCpm())
                .setParameter("conversions", adsetEntity.getConversions())
                .setParameter("cpi", adsetEntity.getCpi())
                .setParameter("adsetId", adsetEntity.getAdsetId())
                .setParameter("accountId", adsetEntity.getAccountId())
                .setParameter("time", adsetEntity.getTime())
                .executeUpdate();*/
        session.getTransaction().commit();
        session.close();
    }
    public boolean isDateInTodayAdsets(Date date, String adsetId) {
        Session session = sessionFactory.openSession();
        try {
            session.createQuery("from AdsetEntity where adset_id=:adsetId and date=:date", AbstractAdsetEntity.class)
                    .setParameter("adsetId", adsetId)
                    .setParameter("date", date).getSingleResult();
            session.close();
            return true;
        } catch (NoResultException e) {
            session.close();
            System.out.println("No adset with this date in db");
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public AbstractAdsetEntity isDateInAdsets(Date date, String adsetId, int accountId, String campaignId) {
        Session session = sessionFactory.openSession();
        try {
            AbstractAdsetEntity adsetEntity = session.createQuery("from AbstractAdsetEntity where adset_id=:adsetId and date=:date and account_id=:accountId and campaign_id=:campaignId", AbstractAdsetEntity.class)
            .setParameter("adsetId", adsetId)
            .setParameter("date", date)
            .setParameter("accountId", accountId)
            .setParameter("campaignId", campaignId)
            .getSingleResult();
            session.close();
            return adsetEntity;
        } catch (NoResultException e) {
            session.close();
            System.out.println("No adset with this date in db");
            return null;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isDateInAdsets(Date date, int accountId, String campaignId) {
        System.out.println(date);
        Session session = sessionFactory.openSession();
        try {
            session.createQuery("from AbstractAdsetEntity where date=:date and account_id=:accountId and campaign_id=:campaignId", AbstractAdsetEntity.class)
                    .setParameter("date", date)
                    .setParameter("accountId", accountId)
                    .setParameter("campaignId", campaignId)
                    .getSingleResult();
            session.close();
            return true;
        } catch (NoResultException e) {
            session.close();
            System.out.println("No adset with this date in db");
            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public CheetahTokenEntity getToken(int accountId) {
        Session session = sessionFactory.openSession();
        CheetahTokenEntity entity = session.createQuery("from CheetahTokenEntity where account_id=:accountId", CheetahTokenEntity.class)
                .setParameter("accountId", accountId).getSingleResult();
        session.close();
        return entity;
    }

    public List<AccountEntity> getAccounts(String type) {
        Session session = sessionFactory.openSession();
        List<AccountEntity> accountEntities = session.createQuery("from AccountEntity acc where acc.type=:statisticsType", AccountEntity.class)
                .setParameter("statisticsType", type).getResultList();
        session.close();
        return accountEntities;
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static synchronized MySQLAdsetDaoImpl getInstance() {
        if (instance == null) instance = new MySQLAdsetDaoImpl();
        return instance;
    }
}
