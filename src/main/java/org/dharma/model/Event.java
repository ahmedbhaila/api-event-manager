package org.dharma.model;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Event {
	private String id;
	
	@NotNull
	private String name;
	
	@NotNull
	private String description;
	
	private String createdby;
	
	@NotNull
	private String location;
	
	private String geoLocation;
	
	@NotNull
	//@DateTimeFormat(iso = ISO.DATE_TIME)
	private String dateTime;
	
	@URL
	private String webUrl;
	
	private String photo;
	
	@NotNull
	private String isPublic;
}
