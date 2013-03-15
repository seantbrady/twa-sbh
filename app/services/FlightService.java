package services;

import binders.OvernightsBinder;
import binders.ParkingEventReportBinder;
import com.avaje.ebean.Ebean;
import models.ParkingEvent;
import models.TrafficLog;
import org.joda.time.DateTime;
import org.supercsv.cellprocessor.FmtDate;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ParseDate;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvBeanWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvBeanWriter;
import org.supercsv.prefs.CsvPreference;
import play.Play;

import java.io.*;
import java.util.List;

public class FlightService {

    public void execute() {
        clearAllData();
        loadTrafficLogs();
        createFlights();
        generateParkingEventsReport();
        generateOvernightsReport();
    }

    public void clearAllData() {

        Ebean.createSqlUpdate("SET REFERENTIAL_INTEGRITY FALSE").execute();

        final List<ParkingEvent> parkingEvents = ParkingEvent.find.all();
        for (ParkingEvent parkingEvent : parkingEvents) {
            parkingEvent.delete();
        }

        final List<TrafficLog> all = TrafficLog.find.all();
        for (TrafficLog trafficLog : all) {
            trafficLog.delete();
        }

        Ebean.createSqlUpdate("SET REFERENTIAL_INTEGRITY TRUE").execute();

    }

    public void loadTrafficLogs() {
        final File file = Play.application().getFile("traffic.csv");
        if (file.exists()) {
            System.out.println("found it");
        } else {
            System.out.println("can't find it");
        }

        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the values to the bean (names must match)
            final String[] header = beanReader.getHeader(true);
            final CellProcessor[] processors = getProcessors();

            TrafficLog log;
            while( (log = beanReader.read(TrafficLog.class, header, processors)) != null ) {
                log.save();
                System.out.println(String.format("lineNo=%s, rowNo=%s, log=%s", beanReader.getLineNumber(),
                        beanReader.getRowNumber(), log.toString()));
            }

            final int rowCount = TrafficLog.find.where().findRowCount();
            System.out.println(rowCount);

        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if( beanReader != null ) {
                try {
                    beanReader.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }


    }

    private static CellProcessor[] getProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // Aircraft
                new ParseDate("MM/dd/yy"), // Date
                new ParseDate("H:m"), //time
                new NotNull(), //A/D
                new Optional() // company
        };

        return processors;
    }

    public void createFlights() {
        final List<TrafficLog> arrivals = TrafficLog.find.where().eq("type", "A").orderBy("dateTime").findList();
        Integer numParkingEvents = 0;
        Integer numMismatchedArrivals = 0;
        Integer numMismatchedDepartures = 0;

        final List<ParkingEvent> parkingEvents = ParkingEvent.find.all();
        for (ParkingEvent parkingEvent : parkingEvents) {
            parkingEvent.delete();
        }

        for (TrafficLog arrival : arrivals) {
            final List<TrafficLog> departures = TrafficLog.find.where()
                    .eq("aircraft", arrival.aircraft)
                    .eq("type", "D")
                    .ge("dateTime", arrival.dateTime)
                    .orderBy("dateTime")
                    .findList();
            if (departures.size() > 0) {
                ParkingEvent parkingEvent = new ParkingEvent(arrival, departures.get(0));
                parkingEvent.save();
                arrival.parkingEvent = parkingEvent;
                arrival.save();
                departures.get(0).parkingEvent = parkingEvent;
                departures.get(0).save();
                numParkingEvents++;
            } else {
                ParkingEvent parkingEvent = new ParkingEvent(arrival, null);
                parkingEvent.save();
                arrival.parkingEvent = parkingEvent;
                System.out.println("No match found for: " + arrival.toString());
                numMismatchedArrivals++;
            }

            System.out.println("Flights: " + numParkingEvents + " Mismatched Arrivals: " + numMismatchedArrivals + " Mismatched Departures: " + numMismatchedDepartures);
        }

        final List<TrafficLog> departures = TrafficLog.find.where().eq("type", "D").isNull("parkingEvent").findList();
        for (TrafficLog departure : departures) {
            ParkingEvent parkingEvent = new ParkingEvent(null, departure);
            parkingEvent.save();
            departure.parkingEvent = parkingEvent;
            departure.save();

            numMismatchedDepartures++;

            System.out.println("Parking Events: " + numParkingEvents + " Mismatched Arrivals: " + numMismatchedArrivals + " Mismatched Departures: " + numMismatchedDepartures);

        }


    }

    public void generateParkingEventsReport() {
        final List<ParkingEvent> parkingEvents = ParkingEvent.find.all();

        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter("target/parking.csv"),
                    CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the bean values to each column (names must match)
            final String[] header = new String[] { "aircraft", "company", "arrival", "departure", "minutes"};
            final CellProcessor[] processors = getFlightReportProcessors();

            // write the header
            beanWriter.writeHeader(header);

            // write the beans
            for(final ParkingEvent parkingEvent : parkingEvents) {
                final ParkingEventReportBinder binder = new ParkingEventReportBinder(parkingEvent);
//                System.out.println(Json.toJson(binder));
                beanWriter.write(binder, header, processors);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if( beanWriter != null ) {
                try {
                    beanWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }

    private static CellProcessor[] getFlightReportProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new NotNull(), // aircraft
                new Optional(), // company
                new Optional(new FmtDate("MM/dd/yyyy HH:mm")), // arrival
                new Optional(new FmtDate("MM/dd/yyyy HH:mm")), // departure
                new Optional() // minutes
        };

        return processors;
    }


    public void generateOvernightsReport() {
        DateTime startDate = new DateTime(2012, 11, 1, 0, 0, 0, 0);
        DateTime endDate = new DateTime(2013, 3, 9, 0, 0, 0, 0);

        ICsvBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvBeanWriter(new FileWriter("target/overnights.csv"),
                    CsvPreference.STANDARD_PREFERENCE);

            // the header elements are used to map the bean values to each column (names must match)
            final String[] header = new String[] { "date", "aircraft"};
            final CellProcessor[] processors = getOvernightsProcessors();

            // write the header
            beanWriter.writeHeader(header);

            // write the beans
            DateTime current = startDate.withTime(23, 59, 59, 99);
            while (current.isBefore(endDate)) {
                final int rowCount = ParkingEvent.find.where()
                        .isNotNull("arrival")
                        .isNotNull("departure")
                        .lt("arrival.dateTime", current)
                        .gt("departure.dateTime", current)
                        .findRowCount();

                final OvernightsBinder binder = new OvernightsBinder();
                binder.date = current.toDate();
                binder.aircraft = rowCount;
//                System.out.println(Json.toJson(binder));
                beanWriter.write(binder, header, processors);
                current = current.plusDays(1);
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if( beanWriter != null ) {
                try {
                    beanWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }

    private static CellProcessor[] getOvernightsProcessors() {

        final CellProcessor[] processors = new CellProcessor[] {
                new FmtDate("MM/dd/yyyy"), // midnight
                new Optional() // number of aircraft
        };

        return processors;
    }
}
