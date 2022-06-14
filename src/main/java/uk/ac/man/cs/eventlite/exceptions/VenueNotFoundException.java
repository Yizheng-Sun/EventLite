package uk.ac.man.cs.eventlite.exceptions;

public class VenueNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5016812401135889608L;

	private long id;

	public VenueNotFoundException(long id) {
		super("Could not find venue " + id);

		this.id = id;
	}

	public long getId() {
		return id;
	}
}
