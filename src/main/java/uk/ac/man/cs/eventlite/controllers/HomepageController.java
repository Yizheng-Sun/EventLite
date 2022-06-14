package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;

@Controller
@RequestMapping(value = "/", produces = { MediaType.TEXT_HTML_VALUE })
public class HomepageController {
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoieWl6aGVuZ3N1biIsImEiOiJjbDI3czV3cG4wMmkwM2tvNnpxNGhtcGZ3In0.mir-soa2MLc8bauG9zw1ug";
	final List<List<Double>> AllEvents = new ArrayList<List<Double>>();
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	
	@GetMapping
	public String getAllEvents(Model model) throws InterruptedException, JsonProcessingException {
		List<Event> Next3Events = new ArrayList<Event>();
		int i=0;
		for(Event event: eventService.findAll()) {
			if((i < 3) && (event.getDate().isAfter(LocalDate.now()))) {
				Next3Events.add(event);
				i++;
			}
		}
		Iterable<Event> next3Events = Next3Events;
		
		List<Event> UpEvents = new ArrayList<Event>();
		for(Event event: eventService.findAll()) {
			if((event.getDate().isAfter(LocalDate.now()))) {
				UpEvents.add(event);
			}
		}
		Iterable<Event> upEvents = UpEvents;
		
		List<Double> nextEventsLat = new ArrayList<>();
		List<Double> nextEventsLon = new ArrayList<>();
		List<String> nextEventNames = new ArrayList<>();
		List<String> nextEventDates = new ArrayList<>();
		List<String> nextEventTimes = new ArrayList<>();
		
		
		for(Event e:upEvents) {
			nextEventsLat.add(e.getVenue().getLat());
			nextEventsLon.add(e.getVenue().getLon());
			nextEventNames.add(e.getName());
			nextEventDates.add(e.getDate().toString());
			try {
			nextEventTimes.add(e.getTime().toString());
			}catch(Exception e1) {
				;
			}
			
		}
		Iterable<Double> Lat = nextEventsLat;
		Iterable<Double> Lon = nextEventsLon;
		
		model.addAttribute("nextEventsLat", Lat);
		model.addAttribute("nextEventsLon", Lon);
		model.addAttribute("nextEventNames", nextEventNames);
		model.addAttribute("nextEventDates", nextEventDates);
		model.addAttribute("nextEventTimes", nextEventTimes);

		
		model.addAttribute("events", next3Events);
//		model.addAttribute("localDate", LocalDate.now());
		model.addAttribute("venues", venueService.findTopVenues());
//		model.addAttribute("point", str_coor);
		return "home/index";
	}
	
	}