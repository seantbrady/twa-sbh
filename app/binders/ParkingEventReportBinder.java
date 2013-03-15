package binders;

import models.ParkingEvent;
import org.joda.time.Duration;

import java.util.Date;

public class ParkingEventReportBinder {

    public String aircraft;

    public String company;

    public Date arrival;

    public Date departure;

    public Long minutes;

    public ParkingEventReportBinder(ParkingEvent parkingEvent) {
        if (parkingEvent.departure != null) {
            this.aircraft = parkingEvent.departure.aircraft;
            this.company = parkingEvent.departure.company;
            this.departure = parkingEvent.departure.dateTime.toDate();
        }
        if (parkingEvent.arrival != null) {
            this.aircraft = parkingEvent.arrival.aircraft;
            this.company = parkingEvent.arrival.company;
            this.arrival = parkingEvent.arrival.dateTime.toDate();
        }
        if (arrival != null && departure != null) {
            this.minutes = new Duration(this.arrival.getTime(), this.departure.getTime()).getStandardMinutes();
        }
    }
}
