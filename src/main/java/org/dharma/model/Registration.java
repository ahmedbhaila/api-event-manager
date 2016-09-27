package org.dharma.model;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Registration {
	
	@NotNull
	private String userId;
	@NotNull
	private String eventId;
	
	private String createdBy;
	private String id;
}
