package online.omnia.mailparser.zoho.zohoentities;

/**
 * Created by lollipop on 05.08.2017.
 */
public class ZohoMessageData {
    private String messageId;
    private String content;

    public ZohoMessageData(String messageId, String content) {
        this.messageId = messageId;
        this.content = content;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getContent() {
        return content;
    }
}
