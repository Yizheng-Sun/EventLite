package uk.ac.man.cs.eventlite.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;

import uk.ac.man.cs.eventlite.entities.Event;

public interface EventRepository extends CrudRepository<Event, Long> {
	List<Event> findByOrderByDateAscTimeAscNameAsc();
	
	Optional<Event> findById (long id);
	
	List<Event> findEventsByNameContainingIgnoreCase(String name, Sort sortByNameAscending);

	Event findByName(String name);
}
