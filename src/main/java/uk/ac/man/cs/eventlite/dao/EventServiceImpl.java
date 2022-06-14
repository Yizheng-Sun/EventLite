package uk.ac.man.cs.eventlite.dao;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import uk.ac.man.cs.eventlite.entities.Event;

@Service
@Transactional
public class EventServiceImpl implements EventService {
	@Autowired 
	private EventRepository eventRepository;
	
	private Sort sortByNameAscending = Sort.by("Name").ascending();

	@Override
	public long count() {
		return eventRepository.count();
	}
	@Override
	public boolean existsById(long id) {
		return eventRepository.existsById(id);
	}

	@Override
	public Iterable<Event> findAll() {
		return eventRepository.findByOrderByDateAscTimeAscNameAsc();
	}
	
	@Override
	public Event save(Event event) {
		return eventRepository.save(event);
	}
	
	@Override
	public Event deleteById(long id) {
		eventRepository.deleteById(id);
		return new Event();
	}
	
	@Override
	public Optional<Event> findById(long id) {
		return eventRepository.findById(id);
	}
	
	@Override
	public Event update(Event event) {
		return save(event);
	}
	@Override
	public List<Event> findEventsByNameContaining(String name) {
		return eventRepository.findEventsByNameContainingIgnoreCase(name, sortByNameAscending);
	}
	@Override
	public Event findByName(String name) {
		return eventRepository.findByName(name);
	}
}
