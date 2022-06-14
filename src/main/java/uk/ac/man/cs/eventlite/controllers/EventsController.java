package uk.ac.man.cs.eventlite.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;
import uk.ac.man.cs.eventlite.exceptions.EventNotFoundException;
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/events", produces = { MediaType.TEXT_HTML_VALUE })
public class EventsController {
	private static final LocalTime NULL = null;
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoieWl6aGVuZ3N1biIsImEiOiJjbDI3czV3cG4wMmkwM2tvNnpxNGhtcGZ3In0.mir-soa2MLc8bauG9zw1ug";
	private final String[] str_coor = new String[2];
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;
	
	private Boolean twitter_success = false;
	private String tweet_str = "";
	private Twitter twitter;
	private Boolean sign_in_twitter = false;
	private Boolean empty_tweet = false;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String eventNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());

		return "events/not_found";
	}

	@GetMapping("/{id}")
	public String getEvent(@PathVariable("id") long id, Model model) throws EventNotFoundException {

		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("event", event);
		return "events/detail";
	}
	
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") long id) throws InterruptedException, TwitterException {
		Event event = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("event", event);
		Venue venue = event.getVenue();
		model.addAttribute("coor1", venue.getLon());
		model.addAttribute("coor2", venue.getLat());
		model.addAttribute("venue_name", venue.getName());
		model.addAttribute("name", event.getName());
		model.addAttribute("date", event.getDate());
		model.addAttribute("time", event.getTime());
		if(twitter_success) {
			model.addAttribute("success","Your tweet: "+tweet_str+" was posted");
			twitter_success = false;
		}
		if(empty_tweet) {
			model.addAttribute("empty_tweet", "You can't share an empty tweet");
			empty_tweet = false;
		}
		
		return "events/detail";
	}

	@GetMapping
	public String getAllEvents(Model model) throws TwitterException {
		model.addAttribute("events", eventService.findAll());
		model.addAttribute("localDate", LocalDate.now());
		if(!sign_in_twitter) {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey("os8ZyLjuLRfxSG6GTRRQmOc4T")
			  .setOAuthConsumerSecret("tOtq7RY02EfSgv9CJbUtKkh52EW1leWH6rnXQW7OifywfMS8gw")
			  .setOAuthAccessToken("1517904970422665216-dKDdUM6H1oBwDU7nQOA02AYSljPIDq")
			  .setOAuthAccessTokenSecret("4VCfE7TXttQOCoqG5ajJa1iyPPzUDUYgmAP9b6qQn1MBL");
			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
			sign_in_twitter = true;
		}
		
		Iterable<Status> statuses = twitter.getHomeTimeline();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
		
		List<String> tweets = new ArrayList<>();
		for (Status status : statuses) {
	        tweets.add(dateFormat.format(status.getCreatedAt())+"--"+status.getText());
	    }
		
		Iterable<Event> events = eventService.findAll();
		List<Double> nextEventsLat = new ArrayList<>();
		List<Double> nextEventsLon = new ArrayList<>();
		List<String> nextEventNames = new ArrayList<>();
		List<String> nextEventDates = new ArrayList<>();
		List<String> nextEventTimes = new ArrayList<>();
		
		for(Event e:events) {
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
		model.addAttribute("EventsLat", Lat);
		model.addAttribute("EventsLon", Lon);
		model.addAttribute("nextEventNames", nextEventNames);
		model.addAttribute("nextEventDates", nextEventDates);
		model.addAttribute("nextEventTimes", nextEventTimes);
		
		List<Status> Next5Tweets = new ArrayList<Status>();
		int i=0;
		for(Status s: statuses) {
			if((i < 5)) {
				Next5Tweets.add(s);
				i++;
			}
		}
		Iterable<Status> next5Tweets = Next5Tweets;
		
		model.addAttribute("tweets", tweets);
		model.addAttribute("timeline", next5Tweets);		
		return "events/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteEvent(@PathVariable("id") long id, RedirectAttributes redirectAttrs) {
		if (!eventService.existsById(id)) {
			throw new EventNotFoundException(id);
		}

		eventService.deleteById(id);

		return "redirect:/events";
	}
	
	@GetMapping("/search")
	public String search(Model model,  @RequestParam(value = "search") String name) {
		List<Event> events = eventService.findEventsByNameContaining(name);
		if(events == null) return "events/not_found";
		model.addAttribute("events", events);
		return "events/index";
	}
	
	@GetMapping("/add")
	public String add(Model model) {
		if (!model.containsAttribute("event")) {
			model.addAttribute("event", new Event());
			model.addAttribute("venues", venueService.findAll());
		}
		return "events/add";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String add2(
			@RequestBody @Valid @ModelAttribute Event event,
			BindingResult errors,
			Model model,
			RedirectAttributes redirectAttrs){

		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/add";
		}
		
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "New event added.");
		return "redirect:/events";
	}
	
	@GetMapping("/update/{id}")
	public String update(Model model, @PathVariable("id") long id) {
			Event e = eventService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
			model.addAttribute("event", e);
			model.addAttribute("venues", venueService.findAll());

		return "events/update";
	}
	
	@PostMapping(value = "/update/{id}",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String update2(
			@RequestBody @Valid @ModelAttribute Event event,
			BindingResult errors,
			Model model,
			RedirectAttributes redirectAttrs,
			@PathVariable("id") long id){
		event.setId(id);
		if (errors.hasErrors()) {
			model.addAttribute("event", event);
			model.addAttribute("venues", venueService.findAll());
			return "events/update";
		}
		
		eventService.save(event);
		redirectAttrs.addFlashAttribute("ok_message", "event updated.");
		return "redirect:/events/detail/"+id;
	}
	

	@PostMapping(value = "/share/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String share_twitter(Model model,
			@RequestParam("share_twitter") String share,
			@PathVariable("id") long id) throws TwitterException, InterruptedException {
			if(share=="") {
				empty_tweet = true;
			}else {
			Status status = twitter.updateStatus(share);
		    twitter_success = true;
		    tweet_str = status.getText();}
			return  "redirect:/events/detail/"+id;
	}
}
