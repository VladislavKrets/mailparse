package online.omnia.mailparser.zoho.dao;

import online.omnia.mailparser.zoho.daoentities.EmailAccessEntity;
import online.omnia.mailparser.zoho.daoentities.EmailSuccessEntity;

import java.util.List;

/**
 * Created by lollipop on 08.08.2017.
 */
public interface MySQLDao {
    List<EmailAccessEntity> getMailsByCheck(int check);
    EmailSuccessEntity getEmailSuccesByMessageId(String messageId);
    void addNewEmailSuccess(EmailSuccessEntity emailSuccessEntity);
    List<EmailSuccessEntity> getEmailSuccess(int firstSuccessValue, int secondSuccessValue);
    void updateSuccessEmtity(EmailSuccessEntity emailSuccessEntity);
}
