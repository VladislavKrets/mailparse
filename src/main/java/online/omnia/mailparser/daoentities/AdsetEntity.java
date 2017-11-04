package online.omnia.mailparser.daoentities;

import javax.persistence.Column;
import java.sql.Time;

/**
 * Created by lollipop on 31.08.2017.
 */
public class AdsetEntity /*extends AbstractAdsetEntity*/{
    @Column(name = "time")
    private Time time;

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }
}
