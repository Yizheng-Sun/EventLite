package uk.ac.man.cs.eventlite.dao;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;


public interface VenueRepository extends CrudRepository<Venue, Long> {
	Optional<Venue> findById (long id);
	
	List<Venue> findByOrderByNameAsc();
	
	@Query("select v from Venue v, Event e where v.id = e.venue group by v.id order by count(e.id) desc")
    public List<Venue> findTopVenues(Pageable limit);

	@Query("select count(e.id) from Venue v, Event e where v.id = :vid and e.venue = :vid")
    public int countEvents(@Param("vid") long id);

	List<Venue> findVenuesByNameContainingIgnoreCase(String name);

	@Query("select e from Venue v, Event e where v.id = :vid and e.venue = v.id")
	public List<Event> findVenueAllEvent(@Param("vid") long id);

	Venue findByName(String name);
	
	@Query("select e from Venue v, Event e where v.id = :vid and e.venue = v.id and e.date >= CURDATE() order by e.date asc ")
	public List<Event> nextThreeEventsOfVenue(@Param("vid") long id, Pageable limit);
}

