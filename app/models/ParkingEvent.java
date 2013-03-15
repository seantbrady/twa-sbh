package models;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class ParkingEvent extends Model {

    @Id
    public Long id;

    @OneToOne
    public TrafficLog arrival;

    @OneToOne
    public TrafficLog departure;

    public static Finder<Long, ParkingEvent> find = new Finder(Long.class, ParkingEvent.class);

    public ParkingEvent(TrafficLog arrival, TrafficLog departure) {
        this.arrival = arrival;
        this.departure = departure;
    }
}
