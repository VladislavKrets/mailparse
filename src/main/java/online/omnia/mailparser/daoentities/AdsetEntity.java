package online.omnia.mailparser.daoentities;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by lollipop on 04.08.2017.
 */
@Entity
@Table(name = "stat_cheetah_email")
public class AdsetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "account_id")
    private int accountId;
    @Column(name = "account_name")
    private String accountName;
    @Column(name = "adset_id")
    private String adsetId;
    @Column(name = "date")
    private Date date;
    @Column(name = "adset_name")
    private String adsetName;
    @Column(name = "CTR")
    private double ctr;
    @Column(name = "impressions")
    private int impressions;
    @Column(name = "spent")
    private double spent;
    @Column(name = "clicks")
    private int clicks;
    @Column(name = "CR")
    private double cr;
    @Column(name = "CPC")
    private double cpc;
    @Column(name = "CPM")
    private double cpm;
    @Column(name = "conversions")
    private int conversions;
    @Column(name = "CPI")
    private double cpi;

    public AdsetEntity() {
    }


    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public void setCr(double cr) {
        this.cr = cr;
    }

    public void setCpc(double cpc) {
        this.cpc = cpc;
    }

    public void setCpm(double cpm) {
        this.cpm = cpm;
    }

    public void setConversions(int conversions) {
        this.conversions = conversions;
    }

    public void setCpi(double cpi) {
        this.cpi = cpi;
    }

    public int getId() {
        return id;
    }

    public int getAccountId() {
        return accountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public int getClicks() {
        return clicks;
    }

    public double getCr() {
        return cr;
    }

    public double getCpc() {
        return cpc;
    }

    public double getCpm() {
        return cpm;
    }

    public int getConversions() {
        return conversions;
    }

    public double getCpi() {
        return cpi;
    }

    public String getAdsetId() {
        return adsetId;
    }

    public Date getDate() {
        return date;
    }

    public String getAdsetName() {
        return adsetName;
    }

    public double getCtr() {
        return ctr;
    }

    public int getImpressions() {
        return impressions;
    }

    public double getSpent() {
        return spent;
    }

    public void setAdsetId(String adsetId) {
        this.adsetId = adsetId;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAdsetName(String adsetName) {
        this.adsetName = adsetName;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public void setSpent(double spent) {
        this.spent = spent;
    }

    @Override
    public String toString() {
        return "AdsetEntity{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", accountName='" + accountName + '\'' +
                ", adsetId='" + adsetId + '\'' +
                ", date=" + date +
                ", adsetName='" + adsetName + '\'' +
                ", ctr=" + ctr +
                ", impressions=" + impressions +
                ", spent=" + spent +
                ", clicks=" + clicks +
                ", cr=" + cr +
                ", cpc=" + cpc +
                ", cpm=" + cpm +
                ", conversions=" + conversions +
                ", cpi=" + cpi +
                '}';
    }
}