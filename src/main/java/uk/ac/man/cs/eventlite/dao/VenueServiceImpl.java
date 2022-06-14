package uk.ac.man.cs.eventlite.dao;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.man.cs.eventlite.entities.Event;
import uk.ac.man.cs.eventlite.entities.Venue;

@Service
@Transactional
public class VenueServiceImpl implements VenueService {
	
	@Autowired
	private VenueRepository venueRepository;

	@Override
	public long count() {
		return venueRepository.count();
	}
	@Override
	public boolean existsById(long id) {
		return venueRepository.existsById(id);
	}

	@Override
	public Iterable<Venue> findAll() {
		return venueRepository.findByOrderByNameAsc();
	}
	
	@Override
	public Venue save(Venue venue) {
		return venueRepository.save(venue);
	}
	
	@Override
	public Venue update(Venue venue) {
		return save(venue);
	}
	
	@Override
	public Venue delete(Venue venue) {
		venueRepository.delete(venue);
		return new Venue();
	}

	@Override
	public Venue deleteById(long id) {
		venueRepository.deleteById(id);
		return new Venue();
	}
	
	@Override
	public Optional<Venue>  findById(long id) {
		return venueRepository.findById(id);
	}
	
	@Override
	public Venue findByName(String name) {
		return venueRepository.findByName(name);
	}

	@Override
	public List<Venue> findTopVenues() {
		Pageable limit = PageRequest.of(0, 3);
		return venueRepository.findTopVenues(limit);
	}
	@Override
	public List<Venue> findVenuesByNameContaining(String name) {
		return venueRepository.findVenuesByNameContainingIgnoreCase(name);
	}

	@Override
	public List<Event> findVenueAllEvent(long id){
		return venueRepository.findVenueAllEvent(id);
	}

	@Override
	public int countEvents(long id){
		return venueRepository.countEvents(id);
	}
	
	@Override
	public Iterable<Event> nextThreeEventsOfVenue(long id){
		Pageable limit = PageRequest.of(0, 3);
		return venueRepository.nextThreeEventsOfVenue(id, limit);
	}
}
