package online.omnia.mailparser;

import java.util.Date;

/**
 * Created by lollipop on 04.08.2017.
 */
public class AdEntity {
    private String number;
    private Date date;
    private String name;
    private String ctr;
    private String impressions;
    private String spent;

    public AdEntity() {
    }

    public AdEntity(String number, Date date, String name,
                    String ctr, String impressions, String spent) {
        this.number = number;
        this.date = date;
        this.name = name;
        this.ctr = ctr;
        this.impressions = impressions;
        this.spent = spent;
    }

    public String getNumber() {
        return number;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getCtr() {
        return ctr;
    }

    public String getImpressions() {
        return impressions;
    }

    public String getSpent() {
        return spent;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCtr(String ctr) {
        this.ctr = ctr;
    }

    public void setImpressions(String impressions) {
        this.impressions = impressions;
    }

    public void setSpent(String spent) {
        this.spent = spent;
    }

    @Override
    public String toString() {
        return "AdEntity{" +
                "number='" + number + '\'' +
                ", date=" + date +
                ", name='" + name + '\'' +
                ", ctr='" + ctr + '\'' +
                ", impressions='" + impressions + '\'' +
                ", spent='" + spent + '\'' +
                '}';
    }
}
