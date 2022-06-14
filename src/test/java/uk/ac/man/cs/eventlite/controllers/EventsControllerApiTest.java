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
@WebMvcTest(EventsControllerApi.class)
@Import({ Security.class, EventModelAssembler.class, VenueModelAssembler.class })
public class EventsControllerApiTest {

	private final static String BAD_ROLE = "USER";

	@Autowired
	private MockMvc mvc;

	@MockBean
	private EventService eventService;
	
    @Test
    public void getIndexWhenNoEvents() throws Exception {
        when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());

        mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(1)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

        verify(eventService).findAll();
    }

    @Test
    public void getIndexWithEvents() throws Exception {
        Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
        when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(e));

        mvc.perform(get("/api/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(handler().methodName("getAllEvents")).andExpect(jsonPath("$.length()", equalTo(2)))
                .andExpect(jsonPath("$._links.self.href", endsWith("/api/events")));

        verify(eventService).findAll();
    }

    @Test
    public void getEventNotFound() throws Exception {
        mvc.perform(get("/api/events/99").accept(MediaType.APPLICATION_JSON)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("event 99"))).andExpect(jsonPath("$.id", equalTo(99)))
                .andExpect(handler().methodName("getEvent"));
    }
    
    @Test
    public void getEventFound() throws Exception {
    	Event e = new Event();
        Venue v = new Venue();
        e.setId(0);
        e.setName("Event");
        e.setDate(LocalDate.now());
        e.setTime(LocalTime.now());
        e.setVenue(v);
    	when(eventService.findById(1)).thenReturn(Optional.of(e));
    	mvc.perform(get("/api/events/1").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
        .andExpect(handler().methodName("getEvent"));
    }
}