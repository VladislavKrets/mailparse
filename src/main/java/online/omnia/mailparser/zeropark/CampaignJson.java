package online.omnia.mailparser.zeropark;

/**
 * Created by lollipop on 13.10.2017.
 */
public class CampaignJson {
    private String campaignId;
    private String name;
    private double spent;
    private int conversions;
    private String clickUrl;

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getSpent() {
        return spent;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    public int getConversions() {
        return conversions;
    }

    public void setConversions(int conversions) {
        this.conversions = conversions;
    }

    @Override
    public String toString() {
        return "CampaignJson{" +
                "campaignId='" + campaignId + '\'' +
                ", name='" + name + '\'' +
                ", spent=" + spent +
                ", conversions=" + conversions +
                '}';
    }

    public String getClickUrl() {
        return clickUrl;
    }

    public void setClickUrl(String clickUrl) {
        this.clickUrl = clickUrl;
    }
}
