package uk.ac.man.cs.eventlite.entities;

import java.time.LocalDate;
import java.time.LocalTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "event")
public class Event{

    @Id
    @GeneratedValue
    private long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Future(message="The date must be in the future.")
    @NotNull(message="An event must have a date.")
    private LocalDate date;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime time;

    @Size(max = 255, message = "The name must be shorter than 256 characters.")
    @NotEmpty(message = "name cannot be empty.")
    private String name;

    @Column(length = 500)
    @Size(max = 499, message = "The description must be shorter than 500 characters.")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull(message="An event must have a venue.")
    private Venue venue;
     
    public Event() {
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }
    
    public String getDescription() {
    	return description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Venue getVenue() {
        return this.venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }
    
    public void setDescription(String s) {
    	this.description = s;
    }
    
}