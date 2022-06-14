package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

public interface VenueService {

	public long count();

	public Iterable<Venue> findAll();

	public Venue save( Venue venue);

	Optional<Venue> findById(long id);

	boolean existsById(long id);

	Venue delete(Venue venue);

	Venue deleteById(long id);

	public List<Venue> findTopVenues();

	public Venue update(Venue venue);

	public Venue findByName(String name);

	public List<Event> findVenueAllEvent(long id);

	public  int countEvents(long id);

	public List<Venue> findVenuesByNameContaining(String name);

	public Iterable<Event> nextThreeEventsOfVenue(long id);
}
