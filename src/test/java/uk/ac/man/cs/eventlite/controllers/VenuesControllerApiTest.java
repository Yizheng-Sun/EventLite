package uk.ac.man.cs.eventlite.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.jayway.jsonpath.Option;

import uk.ac.man.cs.eventlite.assemblers.EventModelAssembler;
import uk.ac.man.cs.eventlite.assemblers.VenueModelAssembler;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VenuesControllerApi.class)
@Import({ Security.class, EventModelAssembler.class, VenueModelAssembler.class })
public class VenuesControllerApiTest {

	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;
	@MockBean
	private VenueService venueService;
	
    @Test
    public void getIndexWhenNoVenues() throws Exception {
        when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

        verify(venueService).findAll();
    }

    @Test
    public void getIndexWithVenues() throws Exception {
        Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        v.setName("Test Venue");
        v.setCapacity(100);
        v.setRoadName("Test Road");
        v.setPostcode("M15 9NN");
        v.setAddress();
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

        mvc.perform(get("/api/venues").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllVenues")).andExpect(jsonPath("$.length()", equalTo(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/venues")));

        verify(venueService).findAll();
    }
    
    @Test
    public void getVenueNotFound() throws Exception {
        mvc.perform(get("/api/venues/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("venue 99"))).andExpect(jsonPath("$.id", equalTo(99)))
                .andExpect(handler().methodName("getVenue"));
    }
    
    
    @Test
    public void getVenueFound() throws Exception {
    	Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        v.setName("Test Venue");
        v.setCapacity(100);
        v.setRoadName("Test Road");
        v.setPostcode("M15 9NN");
        v.setAddress();
    	when(venueService.findById(1)).thenReturn(Optional.of(v));
    	mvc.perform(get("/api/venues/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(handler().methodName("getVenue"));
    }
    
    @Test
    public void getVenueThreeEvents() throws Exception {
    	Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        v.setName("Test Venue");
        v.setCapacity(100);
        v.setRoadName("Test Road");
        v.setPostcode("M15 9NN");
        v.setAddress();
    	when(venueService.nextThreeEventsOfVenue(1)).thenReturn(Collections.<Event>singletonList(e));
    	mvc.perform(get("/api/venues/1/next3events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(handler().methodName("venueThreeEvents"));
    }
    
    @Test
    public void getVenueAllEvents() throws Exception {
    	Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        v.setName("Test Venue");
        v.setCapacity(100);
        v.setRoadName("Test Road");
        v.setPostcode("M15 9NN");
        v.setAddress();
    	when(venueService.findVenueAllEvent(1)).thenReturn(Collections.<Event>singletonList(e));
    	mvc.perform(get("/api/venues/1/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(handler().methodName("venueAllEvents"));
    }

	
}