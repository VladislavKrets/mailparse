package online.omnia.mailparser.zoho.zohoentities;

import java.util.Date;

/**
 * Created by lollipop on 05.08.2017.
 */
public class ZohoMessage {
    private String summary;
    private Date sentDateInGMT;
    private String subject;
    private String messageId;
    private String folderId;
    private String sender;
    private Date receivedTime;
    private String fromAddress;

    public ZohoMessage(String summary, Date sentDateInGMT, String subject, String messageId,
                       String folderId, String sender, Date receivedTime, String fromAddress) {
        this.summary = summary;
        this.sentDateInGMT = sentDateInGMT;
        this.subject = subject;
        this.messageId = messageId;
        this.folderId = folderId;
        this.sender = sender;
        this.receivedTime = receivedTime;
        this.fromAddress = fromAddress;
    }

    public String getSummary() {
        return summary;
    }

    public Date getSentDateInGMT() {
        return sentDateInGMT;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getFolderId() {
        return folderId;
    }

    public String getSender() {
        return sender;
    }

    public Date getReceivedTime() {
        return receivedTime;
    }

    public String getFromAddress() {
        return fromAddress;
    }
}
