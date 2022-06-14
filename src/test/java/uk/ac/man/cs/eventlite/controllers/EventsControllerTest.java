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
import java.util.Random;

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
import twitter4j.conf.ConfigurationBuilder;
import uk.ac.man.cs.eventlite.config.Security;
import uk.ac.man.cs.eventlite.dao.EventService;
import uk.ac.man.cs.eventlite.dao.VenueService;
import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@ExtendWith(SpringExtension.class)
@WebMvcTest(EventsController.class)
@Import(Security.class)
public class EventsControllerTest {
	
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
	
	@BeforeEach
	public void setup() {
		MockitoAnnotations.initMocks(this);
		mvc = MockMvcBuilders.standaloneSetup(eventsController).build();
	}

	@Test
	public void getIndexWithEventsAndTwitter() throws Exception {
		LocalTime time1 = LocalTime.of(10, 15);
        LocalDate date1 = LocalDate.of(2023, 3, 15);
		when(venue.getName()).thenReturn("Kilburn Building");
		when(venueService.findAll()).thenReturn(Collections.<Venue>singletonList(venue));
		when(event.getVenue()).thenReturn(venue);
		when(event.getName()).thenReturn("Test");
		when(event.getDate()).thenReturn(date1);
		when(event.getTime()).thenReturn(time1);
		when(eventService.findAll()).thenReturn(Collections.<Event>singletonList(event));

		Status status = mock(Status.class);
		when(status.getText()).thenReturn("It's a nice summer day!");
		TwitterFactory tf = mock(TwitterFactory.class);
		Twitter tw = mock(Twitter.class);
		when(tf.getInstance()).thenReturn(tw);
		when(tw.getHomeTimeline()).thenReturn(null);
		mvc.perform(get("/events").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
				.andExpect(view().name("events/index")).andExpect(handler().methodName("getAllEvents"));
		when(eventService.findById(2)).thenReturn(Optional.of(event));
		when(event.getDate()).thenReturn(LocalDate.of(2000,10,10));
		when(event.getTime()).thenReturn(LocalTime.of(10, 10));
		when(event.getName()).thenReturn("Hello");
        when(event.getVenue()).thenReturn(new Venue());
        mvc.perform(get("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
    	    	.contentType(MediaType.APPLICATION_FORM_URLENCODED));
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
    	mvc.perform(post("/events/share/1").with(user("Rob").roles(Security.ADMIN_ROLE))
    	.contentType(MediaType.APPLICATION_FORM_URLENCODED)
		.param("share_twitter",generatedString)
		.accept(MediaType.TEXT_HTML).with(csrf()))
    	.andExpect(status().isFound())
		.andExpect(handler().methodName("share_twitter"));;
		
		mvc.perform(post("/events/share/1").with(user("Rob").roles(Security.ADMIN_ROLE))
		    	.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("share_twitter","")
				.accept(MediaType.TEXT_HTML).with(csrf()))
		    	.andExpect(status().isFound())
				.andExpect(handler().methodName("share_twitter"));;
    	
        mvc.perform(get("/events/detail/2").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("events/detail")).andExpect(handler().methodName("detail"));
        
        verify(eventService).findById(2);
	}

    
    @Test
    public void getEventNotFound() throws Exception {
        mvc.perform(get("/events/1").accept(MediaType.TEXT_HTML)).andExpect(status().isNotFound())
                .andExpect(view().name("events/not_found")).andExpect(handler().methodName("getEvent"));
        verify(eventService).findById(1);
    }
    
    @Test
    public void getDetaiFound() throws Exception {
		when(eventService.findById(2)).thenReturn(Optional.of(event));
		when(event.getDate()).thenReturn(LocalDate.of(2000,10,10));
		when(event.getTime()).thenReturn(LocalTime.of(10, 10));
		when(event.getName()).thenReturn("Hello");
        when(event.getVenue()).thenReturn(new Venue());
        mvc.perform(get("/events/detail/2").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
                .andExpect(view().name("events/detail")).andExpect(handler().methodName("detail"));
        
        verify(eventService).findById(2);
    }

    @Test
    public void getEventFound() throws Exception{
		when(eventService.findById(2)).thenReturn(Optional.of(event));
		when(event.getDate()).thenReturn(LocalDate.of(2000,10,10));
		when(event.getTime()).thenReturn(LocalTime.of(10, 10));
		when(event.getName()).thenReturn("Hello");
        when(event.getVenue()).thenReturn(new Venue());
        mvc.perform(get("/events/2").accept(MediaType.TEXT_HTML)).andExpect(status().isOk())
        .andExpect(view().name("events/detail")).andExpect(handler().methodName("getEvent"));
        
        verify(eventService).findById(2);
    }
    
//    Test adding a event
    
    @Test
    public void getAddEvent() throws Exception{
    	mvc.perform(get("/events/add").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("events/add"))
		.andExpect(handler().methodName("add"));
    }
    
    @Test
    public void postaddEventFutureDate() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		when(venue.getId()).thenReturn((long) 1);
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Hello")
				.param("date","2023-06-10")
				.param("venue.value", ""+venue.getId())
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("add2")).andExpect(flash().attributeExists("ok_message"));
		verify(eventService).save(arg.capture());
		assertThat("Hello", equalTo(arg.getValue().getName()));
    }
    
    @Test
    public void postaddEventPastDate() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Hello")
				.param("date","2021-06-10")
				.param("venue.value", ""+venue.getId())
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("add2"));
    }
    
    @Test
    public void postaddEventNoVenue() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Hello")
				.param("date","2021-06-10")
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("add2"));
    }
    
    @Test
    public void postaddEventNoDate() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Hello")
				.param("venue.value", ""+venue.getId())
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("add2"));
    }
    
    @Test
    public void postaddEventLongName() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		String longStr = new String(new char[600]).replace('\0', ' ');
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name",longStr)
				.param("date","2023-06-10")
				.param("venue.value", ""+venue.getId())
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("add2"));
    }
    
    @Test
    public void postaddEventLongDescript() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		String longStr = new String(new char[600]).replace('\0', ' ');
		
		mvc.perform(post("/events").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Hello")
				.param("date","2023-06-10")
				.param("venue.value", ""+venue.getId())
				.param("description", longStr)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("add2"));
    }
    
//    Test updating an event
    
    @Test
    public void getUpdateEvent() throws Exception{
		when(eventService.findById(2)).thenReturn(Optional.of(event));
    	mvc.perform(get("/events/update/2").with(user("Rob").roles(Security.ADMIN_ROLE)).accept(MediaType.TEXT_HTML))
		.andExpect(status().isOk()).andExpect(view().name("events/update"))
		.andExpect(handler().methodName("update"));
    }
    
    @Test
    public void postupdateEventFutureDate() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		when(venue.getId()).thenReturn((long) 1);
		
		mvc.perform(post("/events/update/2").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("name","Updated Name")
				.param("date","2023-06-10")
				.param("venue.value", ""+venue.getId())
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("update2")).andExpect(flash().attributeExists("ok_message"));
		verify(eventService).save(arg.capture());
		assertThat("Updated Name", equalTo(arg.getValue().getName()));
    }
    
    @Test
    public void postupdateEventNoName() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.save(any(Event.class))).then(returnsFirstArg());
		String longStr = new String(new char[600]).replace('\0', ' ');
		
		mvc.perform(post("/events/update/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("date","2023-06-10")
				.param("venue.value", ""+venue.getId())
				.param("description", longStr)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isOk()).andExpect(model().hasErrors())
				.andExpect(handler().methodName("update2"));
    }
    
//    Test delete event
    @Test
    public void deleteEvent() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.existsById(1)).thenReturn(true);
		when(eventService.deleteById(1)).thenReturn(event);
		mvc.perform(delete("/events/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isFound()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("deleteEvent"));
		verify(eventService).deleteById(1);
    }
    
    @Test
    public void deleteEventNotExist() throws Exception{
    	ArgumentCaptor<Event> arg = ArgumentCaptor.forClass(Event.class);
		when(eventService.existsById(1)).thenReturn(false);
		mvc.perform(delete("/events/1").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.TEXT_HTML).with(csrf()))
				.andExpect(status().isNotFound())
				.andExpect(handler().methodName("deleteEvent"));
    }
    
//   Test Search
    @Test 
    void searchEventExist() throws Exception{
    	when(eventService.findEventsByNameContaining("A")).thenReturn(Collections.<Event>singletonList(event));
    	mvc.perform(get("/events/search").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("search","A")
				.accept(MediaType.TEXT_HTML).with(csrf()))
    			.andExpect(status().isOk()).andExpect(model().hasNoErrors())
				.andExpect(handler().methodName("search"));
    }
    
    @Test 
    void searchEventNotExist() throws Exception{
    	when(eventService.findEventsByNameContaining("A")).thenReturn(null);
    	mvc.perform(get("/events/search").with(user("Rob").roles(Security.ADMIN_ROLE))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.param("search","A")
				.accept(MediaType.TEXT_HTML).with(csrf()))
    			.andExpect(status().isOk())
				.andExpect(handler().methodName("search"))
				.andExpect(view().name("events/not_found"));
    }
}
