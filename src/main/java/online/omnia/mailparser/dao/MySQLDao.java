package online.omnia.mailparser.dao;

import online.omnia.mailparser.daoentities.AdsetEntity;
import online.omnia.mailparser.daoentities.EmailAccessEntity;
import online.omnia.mailparser.daoentities.EmailSuccessEntity;

import java.util.List;

/**
 * Created by lollipop on 08.08.2017.
 */
public interface MySQLDao {
    List<EmailAccessEntity> getMailsByCheck(int check);
    EmailSuccessEntity getEmailSuccessByMessageId(String messageId);
    void addNewEmailSuccess(EmailSuccessEntity emailSuccessEntity);
    List<EmailSuccessEntity> getEmailSuccess(int firstSuccessValue, int secondSuccessValue);
    void updateSuccessEntity(EmailSuccessEntity emailSuccessEntity);
    void addAdset(AdsetEntity adsetEntity);
    EmailAccessEntity getAccessById(int id);
}