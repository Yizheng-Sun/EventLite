package uk.ac.man.cs.eventlite.exceptions;

public class EventNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5016812401135889608L;

	private long id;

	public EventNotFoundException(long id) {
		super("Could not find event " + id);

		this.id = id;
	}

	public long getId() {
		return id;
	}
}
