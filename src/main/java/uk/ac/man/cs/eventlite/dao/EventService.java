package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventService {

	public long count();

	public Iterable<Event> findAll();
	
	public Event save(Event event);

	boolean existsById(long id);

	Optional<Event> findById(long id);

	Event deleteById(long id);

	Event update(Event event);

	List<Event> findEventsByNameContaining(String name);

	Event findByName(String name);

}
