package uk.ac.man.cs.eventlite.controllers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
import uk.ac.man.cs.eventlite.exceptions.VenueNotFoundException;

@Controller
@RequestMapping(value = "/venues", produces = { MediaType.TEXT_HTML_VALUE })
public class VenuesController {
	String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoieWl6aGVuZ3N1biIsImEiOiJjbDI3czV3cG4wMmkwM2tvNnpxNGhtcGZ3In0.mir-soa2MLc8bauG9zw1ug";
	private final String[] str_coor = new String[2];
	final int[] load_state = new int[2];
	
	@Autowired
	private EventService eventService;

	@Autowired
	private VenueService venueService;

	@ExceptionHandler(EventNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String VenueNotFoundHandler(EventNotFoundException ex, Model model) {
		model.addAttribute("not_found_id", ex.getId());
		return "venues/not_found";
	}

	@GetMapping("/{id}")
	public String getVenue(@PathVariable("id") long id, Model model) throws VenueNotFoundException {

		Venue venue = venueService.findById(id).orElseThrow(() -> new VenueNotFoundException(id));
		model.addAttribute("coor1", venue.getLon());
		model.addAttribute("coor2", venue.getLat());
		model.addAttribute("venue_name", venue.getName());
		List<Event> events = venueService.findVenueAllEvent(id);
		model.addAttribute("venue_events", events);
		model.addAttribute("localDate", LocalDate.now());
		model.addAttribute("venue", venue);
		return "venues/detail";
	}
	
	@GetMapping("/detail/{id}")
	public String detail(Model model, @PathVariable("id") long id) throws InterruptedException {
		Venue venue = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		
		model.addAttribute("coor1", venue.getLon());
		model.addAttribute("coor2", venue.getLat());
		model.addAttribute("venue_name", venue.getName());
		List<Event> events = venueService.findVenueAllEvent(id);
		model.addAttribute("venue", venue);
		model.addAttribute("venue_events", events);
		model.addAttribute("localDate", LocalDate.now());
		return "venues/detail";
	}

	@GetMapping
	public String getAllVenues(Model model) {
		model.addAttribute("venues", venueService.findAll());
		return "venues/index";
	}
	
	@DeleteMapping("/{id}")
	public String deleteVenue(@PathVariable("id") long id,
							  Model model,
							  RedirectAttributes redirectAttrs) {
		if (!venueService.existsById(id)) {
			throw new EventNotFoundException(id);
		}
		int count = venueService.countEvents(id);
		if (count<=0) {
			venueService.deleteById(id);
		}else{
			model.addAttribute("error", "Cannot delete venue with events.");
			Venue venue = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
			model.addAttribute("coor1", venue.getLon());
			model.addAttribute("coor2", venue.getLat());
			model.addAttribute("venue_name", venue.getName());
			List<Event> events = venueService.findVenueAllEvent(id);
			model.addAttribute("venue", venue);
			model.addAttribute("venue_events", events);
			model.addAttribute("localDate", LocalDate.now());
			return "venues/detail";
		}
		return "redirect:/venues";
	}
	
	@GetMapping("/update/{id}")
	public String updateVenue(Model model, @PathVariable("id") long id){
		Venue venue = venueService.findById(id).orElseThrow(() -> new EventNotFoundException(id));
		model.addAttribute("venue", venue);
		return "venues/update";
	}
	
	@PostMapping(value = "/update/{id}",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String update2(
			@RequestBody @Valid @ModelAttribute Venue venue,
			BindingResult errors,
			Model model,
			RedirectAttributes redirectAttrs,
			@PathVariable("id") long id) throws InterruptedException{
		venue.setId(id);
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
			return "venues/update";
		}
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(MAPBOX_ACCESS_TOKEN)
				.query(venue.getAddress())
				.build();

		Thread.sleep(500L);
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		 
				List<CarmenFeature> results = response.body().features();
		 
				if (results.size() > 0) {
				  // Log the first results Point.
				  Point firstResultPoint = results.get(0).center();
				  List<Double> coor = firstResultPoint.coordinates();
				  venue.setlon(coor.get(0));
				  venue.setlat(coor.get(1));
				} else {
				  // No result for your request were found.
				}
			}
		 
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {}
		});
		
//		model.addAttribute("point", str_coor);
		Thread.sleep(500L);

		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "venue updated.");
		return "redirect:/venues/detail/"+id;
	}
	
	@GetMapping("/search")
	public String search(Model model,  @RequestParam("search") String name) {
		List<Venue> venues  = venueService.findVenuesByNameContaining(name);
		if(venues==null) {
			return "venues/not_found";
		}
		model.addAttribute("venues", venues);
		return "venues/index";

	}
	
	@GetMapping("/add")
	public String add(Model model) {
		if (!model.containsAttribute("venue")) {
			model.addAttribute("venue", new Venue());
		}
		return "venues/add";
	}
	
	@PostMapping(consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public String addVenueToDB(
			@RequestBody @Valid @ModelAttribute Venue venue,
			BindingResult errors,
			Model model,
			RedirectAttributes redirectAttrs) throws InterruptedException{
	
		if (errors.hasErrors()) {
			model.addAttribute("venue", venue);
//			model.addAttribute("venues", venueService.findAll());
			return "venues/add";
		}
		MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
				.accessToken(MAPBOX_ACCESS_TOKEN)
				.query(venue.getAddress())
				.build();

		Thread.sleep(500L);
		
		mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
			@Override
			public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
		 
				List<CarmenFeature> results = response.body().features();
		 
				if (results.size() > 0) {
				  // Log the first results Point.
				  Point firstResultPoint = results.get(0).center();
				  List<Double> coor = firstResultPoint.coordinates();
				  venue.setlon(coor.get(0));
				  venue.setlat(coor.get(1));
				} else {
				  // No result for your request were found.
				}
			}
		 
			@Override
			public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {}
		});
		
//		model.addAttribute("point", str_coor);
		Thread.sleep(500L);

		venueService.save(venue);
		redirectAttrs.addFlashAttribute("ok_message", "New venue has been added.");
		return "redirect:/venues";
	}
	
}
