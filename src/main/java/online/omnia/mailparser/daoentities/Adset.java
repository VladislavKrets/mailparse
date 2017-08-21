package online.omnia.mailparser.daoentities;

/**
 * Created by lollipop on 21.08.2017.
 */
public class Adset {
    private String adsetName;
    private String campaignId;

    public Adset(String adsetName, String companyId) {
        this.adsetName = adsetName;
        this.campaignId = companyId;
    }

    public String getAdsetName() {
        return adsetName;
    }

    public String getCampaignId() {
        return campaignId;
    }
}
