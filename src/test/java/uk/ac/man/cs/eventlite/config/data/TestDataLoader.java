package uk.ac.man.cs.eventlite.config.data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Configuration
@Profile("test")
public class TestDataLoader {

	private final static Logger log = LoggerFactory.getLogger(InitialDataLoader.class);
    private final static String[] EVENTNAMES = {"Chess Game","TRPG meeting","Team CourseWork Meeting","Lecture A+B","Collect BRP"};
    private final static String[] venueNames = {"Kilburn Building","McDonald's","Sugden Sports Centre","Alan Turing Building","Accommodation Office"};
    private final static String[] POSTCODE = {"M13 9PL","M12 4AB","M1 7HL","M13 9PY","M13 9WJ"};
    private final static String[] RoadName = {"Kilburn Building University of Manchester, Oxford Rd, Manchester",
    										  "Green, 129 Stockport Rd, Ardwick, Manchester",
    										  "114 Grosvenor St, Manchester",
    										  "Upper Brook St, Manchester",
    										  "Grove House, Oxford Rd, Manchester"};
    private final static String[] DESCRIPTION = {"This is a chess meeting","Play TRPG and eat MC nuggets","Work out and finish team CW","Take this lecture and don't forget the attendence","Collect your BRP please"};
    private final static Double[] Lat = {53.46717142267482,53.46765794560308,53.470896523682775,53.46818295288038,53.462407198987364};
    private final static Double[] Lon = {-2.23434358605667,-2.2174817863763767,-2.2354048088493013,-2.2310502220829656,-2.2300064731010187};
    private final static Integer[] Capacity = {10, 7, 5, 200, 1};
    
    @Autowired
    private EventService eventService;

    @Autowired
    private VenueService venueService;
	    


    @Bean
    CommandLineRunner initDatabase() {
        ArrayList<Venue> venues = new ArrayList<>();
        for(int i = 0 ; i < 5 ; i++) {
        	Venue  v = new Venue();
        	v.setName(venueNames[i]);
        	v.setPostcode(POSTCODE[i]);
        	v.setRoadName(RoadName[i]);
        	v.setAddress();
        	v.setlat(Lat[i]);
        	v.setlon(Lon[i]);
        	v.setCapacity(Capacity[i]);
            venues.add(v);
        }
        return args -> {
            if (venueService.count() > 0) {
                log.info("Database already populated with venues. Skipping venue initialization.");
            } else {
                for(int i = 0 ; i < 5 ; i++) {
                    log.info("Preloading:" + venueService.save(venues.get(i)));
                }
            }

            if (eventService.count() > 0) {
                log.info("Database already populated with events. Skipping event initialization.");
            } else {
                // Build and save initial events here.
                LocalTime time1 = LocalTime.of(10, 15);
                LocalDate date1 = LocalDate.of(2023, 3, 15);
                LocalTime time2 = LocalTime.of(5, 20);
                LocalDate date2 = LocalDate.of(2023, 4, 1);
                LocalTime time3 = LocalTime.of(10, 30);
                LocalDate date3 = LocalDate.of(2023, 3, 10);
                LocalTime time4 = LocalTime.of(14, 30);
                LocalDate date4 = LocalDate.of(2022, 3, 15);
                LocalTime time5 = LocalTime.of(11, 30);
                LocalDate date5 = LocalDate.of(2022, 1, 19);
                LocalTime[] times = {time1, time2, time3, time4, time5};
                LocalDate[] dates = {date1, date2, date3, date4, date5};
                for (int i = 0; i < 5 ; i++) {
                	Event e = new Event();
                	e.setName(EVENTNAMES[i]);
                	e.setDate(dates[i]);
                	e.setTime(times[i]);
                	e.setVenue(venues.get(i));
                	e.setDescription(DESCRIPTION[i]);
                    log.info("Preloading: "+eventService.save(e));
                   }
            }
        };
        }
}
