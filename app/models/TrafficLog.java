package models;

import org.joda.time.DateTime;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

@Entity
public class TrafficLog extends Model {

    @Id
    public Long id;

    public String aircraft;

    public DateTime date;

    public DateTime time;

    public DateTime dateTime;

    public String type;

    public String company;

    @ManyToOne
    public ParkingEvent parkingEvent;

    public static Finder<Long, TrafficLog> find = new Finder(Long.class, TrafficLog.class);

    public TrafficLog() {}

    public void setDate(Date date) {
        this.date = new DateTime(date);
        if (this.dateTime == null) this.dateTime = this.date;
        else this.dateTime = this.dateTime.withDate(this.date.getYear(), this.date.getMonthOfYear(), this.date.getDayOfMonth());
    }

    public void setTime(Date time) {
        this.time = new DateTime(time);
        if (this.dateTime == null) this.dateTime = this.time;
        else this.dateTime = this.dateTime.withTime(this.time.getHourOfDay(), this.time.getMinuteOfHour(), 0, 0);
    }

    @Override
    public String toString() {
        return "TrafficLog{" +
                "id=" + id +
                ", aircraft='" + aircraft + '\'' +
                ", date=" + date +
                ", time=" + time +
                ", dateTime=" + dateTime +
                ", type='" + type + '\'' +
                ", company='" + company + '\'' +
                '}';
    }
}
