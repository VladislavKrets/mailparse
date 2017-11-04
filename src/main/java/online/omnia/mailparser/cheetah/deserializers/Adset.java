package online.omnia.mailparser.cheetah.deserializers;

/**
 * Created by lollipop on 21.08.2017.
 */
public class Adset {
    private String adsetName;
    private String campaignId;
    private String clickUrl;

    public Adset(String adsetName, String companyId, String clickUrl) {
        this.adsetName = adsetName;
        this.campaignId = companyId;
        this.clickUrl = clickUrl;
    }

    public String getAdsetName() {
        return adsetName;
    }

    public String getCampaignId() {
        return campaignId;
    }

    public String getClickUrl() {
        return clickUrl;
    }
}
