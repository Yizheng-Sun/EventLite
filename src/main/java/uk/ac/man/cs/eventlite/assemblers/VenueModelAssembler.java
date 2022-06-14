package uk.ac.man.cs.eventlite.assemblers;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import uk.ac.man.cs.eventlite.controllers.EventsControllerApi;
import uk.ac.man.cs.eventlite.controllers.VenuesControllerApi;
import uk.ac.man.cs.eventlite.entities.Venue;

@Component
public class VenueModelAssembler implements RepresentationModelAssembler<Venue, EntityModel<Venue>> {

	@Override
	public EntityModel<Venue> toModel(Venue venue) {
		return EntityModel.of(venue, linkTo(methodOn(VenuesControllerApi.class).getVenue(venue.getId())).withRel("venue"));
	}
}
