package uk.ac.man.cs.eventlite.controllers;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Optional;

import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.mock;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class VenuesControllerTest {
	
	private final static String BAD_ROLE = "USER";
	public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    @Autowired
    private MockMvc mvc;

    @Mock
    private Event event;

    @Mock
    private Venue venue;

    @MockBean
    private EventService eventService;

    @MockBean
    private VenueService venueService;
	
	@InjectMocks
	private EventsController eventsController;
	@InjectMocks
	private VenuesController venuesController;
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(venuesController).build();
	}
	
	@Test
	public void getIndexWhenNoVenues() throws Exception {
		when(eventService.findAll()).thenReturn(Collections.<Event>emptyList());
		when(venueService.findAll()).thenReturn(Collections.<Venue>emptyList());
		Status status = mock(Status.class);
		when(status.getText()).thenReturn("It's a nice summer day!");
		TwitterFactory tf = mock(TwitterFactory.class);
		Twitter tw = mock(Twitter.class);
		when(tf.getInstance()).thenReturn(tw);
		when(tw.getHomeTimeline()).thenReturn(null);
		
		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk())
		.andExpect(view().name("venues/index"))
		.andExpect(handler().methodName("getAllVenues"));
	}

	@Test
	public void getIndexWithVenues() throws Exception {
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venue.getCapacity()).thenReturn(100);
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		mvc.perform(get("/venues").accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk())
		.andExpect(view().name("venues/index"))
		.andExpect(handler().methodName("getAllVenues"));
	}
 
    
    @Test
    public void getVenueFound() throws Exception{
		when(venueService.findById(1)).thenReturn(Optional.of(venue));
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venue.getCapacity()).thenReturn(100);
		when(venue.getAddress()).thenReturn("Test");
		when(venue.getPostcode()).thenReturn("M15 9NN");
		when(venue.getLon()).thenReturn((double) 0.00);
		when(venue.getLat()).thenReturn((double) 1.00);
		when(venue.getId()).thenReturn((long) 1);
		
        mvc.perform(get("/venues/1").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("venues/detail"))
        .andExpect(handler().methodName("getVenue"));
        
        verify(venueService).findById(1);
    }
    
    @Test
    public void getVenueDetail() throws Exception{
    	when(venueService.findById(1)).thenReturn(Optional.of(venue));
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venue.getCapacity()).thenReturn(100);
		when(venue.getAddress()).thenReturn("Test");
		when(venue.getPostcode()).thenReturn("M15 9NN");
		when(venue.getLon()).thenReturn((double) 0.00);
		when(venue.getLat()).thenReturn((double) 1.00);
		when(venue.getId()).thenReturn((long) 1);
		
        mvc.perform(get("/venues/detail/1").accept(MediaType.TEXT_HTML))
        .andExpect(status().isOk())
        .andExpect(view().name("venues/detail"))
        .andExpect(handler().methodName("detail"));
        
        verify(venueService).findById(1);
    }
    
//    Test adding a event
    @Test
    public void getAddEvent() throws Exception{
    	mvc.perform(get("/venues/add").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("venues/add"))
		.andExpect(handler().methodName("add"));
    }
    
    @Test
    public void postaddVenue() throws Exception{
    	ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		when(venueService.save(any(Venue.class))).then(returnsFirstArg());
		when(venue.getId()).thenReturn((long) 1);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Test Venue")
				.param("capacity","100")
				.param("roadName", "Oxford Rd, Manchester")
				.param("postcode", "M14 4PW")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("addVenueToDB"))
				.andExpect(flash().attributeExists("ok_message"));
		verify(venueService).save(arg.capture());
		assertThat("Test Venue", equalTo(arg.getValue().getName()));
    }
    
    @Test
    public void postaddVenueWithoutName() throws Exception{
    	ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		when(venueService.save(any(Venue.class))).then(returnsFirstArg());
		when(venue.getId()).thenReturn((long) 1);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("capacity","100")
				.param("roadName", "TestRoadName")
				.param("postcode", "M15 9NN")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("addVenueToDB"));
    }
    
    @Test
    public void postaddVenueWithoutCapacity() throws Exception{
    	ArgumentCaptor<Venue> arg = ArgumentCaptor.forClass(Venue.class);
		when(venueService.save(any(Venue.class))).then(returnsFirstArg());
		when(venue.getId()).thenReturn((long) 1);
		
		mvc.perform(post("/venues").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Test Venue")
				.param("roadName", "TestRoadName")
				.param("postcode", "M15 9NN")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("addVenueToDB"));
    }
//    Test updating an event
    
    @Test
    public void getUpdateVenue() throws Exception{
		when(venueService.findById(1)).thenReturn(Optional.of(venue));
    	mvc.perform(get("/venues/update/1").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("venues/update"))
		.andExpect(handler().methodName("updateVenue"));
    }
    
    @Test
    public void postUpdateVenue() throws Exception{
    	when(eventService.findById(2)).thenReturn(Optional.of(event));
    	mvc.perform(post("/venues/update/2").with(user("Rob").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
    	.param("name","Test Venue")
		.param("capacity","100")
		.param("roadName", "Oxford Rd, Manchester")
		.param("postcode", "M14 4PW")
		.accept(MediaType.TEXT_HTML).with(csrf()))
		.andExpect(status().isFound()).andExpect(view().name("redirect:/venues/detail/2"))
		.andExpect(handler().methodName("update2"));
    }
    
    @Test
    public void postUpdateVenueWithoutPostcode() throws Exception{
    	when(eventService.findById(2)).thenReturn(Optional.of(event));
    	mvc.perform(post("/venues/update/2").with(user("Rob").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
    	.param("name","Test Venue")
		.param("capacity","100")
		.param("roadName", "TestRoadName")
		.accept(MediaType.TEXT_HTML).with(csrf()))
    	.andExpect(status().isOk()).andExpect(model().hasErrors())
		.andExpect(handler().methodName("update2"));
    }
    
    @Test
    public void postUpdateVenueWithoutCapacity() throws Exception{
    	when(eventService.findById(2)).thenReturn(Optional.of(event));
    	mvc.perform(post("/venues/update/2").with(user("Rob").roles(Security.ADMIN_ROLE)).contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.param("name","Test Venue")
		.param("roadName", "TestRoadName")
		.param("postcode", "M15 9NN")
		.accept(MediaType.TEXT_HTML).with(csrf()))
    	.andExpect(status().isOk()).andExpect(model().hasErrors())
		.andExpect(handler().methodName("update2"));
    }
//    Test delete event
    @Test
    public void deleteVenue() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(venueService.existsById(1)).thenReturn(true);
		when(venueService.deleteById(1)).thenReturn(venue);
		when(venueService.countEvents(1)).thenReturn((int) 0);
		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("deleteVenue"));
		verify(venueService).deleteById(1);
    }
    
    @Test
    public void deleteVenueWithEvents() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(venueService.existsById(1)).thenReturn(true);
		when(venueService.findById(1)).thenReturn(Optional.of(venue));
		when(venueService.deleteById(1)).thenReturn(venue);
		when(venueService.countEvents(1)).thenReturn((int) 5);
		when(venueService.findVenueAllEvent(1)).thenReturn(Collections.<Event>singletonList(event));
		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk())
				.andExpect(view().name("venues/detail"))
				.andExpect(handler().methodName("deleteVenue"));
    }
    
    @Test
    public void deleteVenueNotExist() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.existsById(1)).thenReturn(false);
		mvc.perform(delete("/venues/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(handler().methodName("deleteVenue"));
    }
    
//   Test Search
    @Test 
    void searchEventExist() throws Exception{
    	when(venueService.findVenuesByNameContaining("A")).thenReturn(Collections.<Venue>singletonList(venue));
    	mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("search","A")
				.accept(MediaType.TEXT_HTML).with(csrf()))
    			.andExpect(status().isOk()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("search"));
    }
    
    @Test 
    void searchEventNotExist() throws Exception{
    	when(venueService.findVenuesByNameContaining("A")).thenReturn(null);
    	mvc.perform(get("/venues/search").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("search","A")
				.accept(MediaType.TEXT_HTML).with(csrf()))
    			.andExpect(status().isOk())
				.andExpect(handler().methodName("search"))
				.andExpect(view().name("venues/not_found"));
    }
    

}
