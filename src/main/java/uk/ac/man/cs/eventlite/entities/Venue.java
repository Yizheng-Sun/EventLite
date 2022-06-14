 package uk.ac.man.cs.eventlite.entities;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Entity
@Table(name="venues")
public class Venue {
	
	@Id
	@GeneratedValue
	private long id;

	@NotNull(message="A venue must have a name.")
	@Size(max = 255, message = "The name of venue must be shorter than 256 characters.")
	private String name;
	
	@NotNull(message="A venue must have a capacity.")
	@Min(value = 1, message = "The capacity must be greater than 0")
	private int capacity;
	
	//Address must contain a road name < 300 chars and a postcode
	private String address;
	
	@NotNull(message="An address must contain a postcode.")
	@Pattern(regexp="[A-Za-z]{1,2}[0-9Rr][0-9A-Za-z]? [0-9][ABD-HJLNP-UW-Zabd-hjlnp-uw-z]{2}",message="The post code should be in right format")
	private String postcode;
	
	@Size(max = 299, message = "The length of the road name should be less than 300")
	private String roadName;
	
	private Double lat;
	private Double lon;

	public Venue() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress() {
		this.address = getRoadName() + "," + getPostcode();
	}
	
	public String getPostcode() {
	    return postcode;
	}

	public void setPostcode(String postcode) {
		address+=postcode;
		this.postcode = postcode;
	}
	
	public String getRoadName() {
	    return roadName;
	}

	public void setRoadName(String roadName) {
		address+=roadName;
		this.roadName = roadName;
	}
	
	public void setlat(Double latin) {
		lat = latin;
	}
	
	public void setlon(Double lonin) {
		lon = lonin;
	}
	
	public Double getLat() {
		return lat;
	}
	
	public Double getLon() {
		return lon;
	}
}
