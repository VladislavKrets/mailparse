package online.omnia.mailparser.zoho.daoentities;

import javax.persistence.*;

/**
 * Created by lollipop on 09.08.2017.
 */
@Entity
@Table(name = "email_success")
public class EmailSuccessEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "message_id")
    private String messageId;
    @Column(name = "success")
    private int success;
    @Column(name = "email_access_id")
    private int emailAccessId;

    public EmailSuccessEntity() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public void setEmailAccessId(int emailAccessId) {
        this.emailAccessId = emailAccessId;
    }

    public int getId() {
        return id;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getSuccess() {
        return success;
    }

    public int getEmailAccessId() {
        return emailAccessId;
    }
}
