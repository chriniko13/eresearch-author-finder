package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuthorNameDto {

	@JsonProperty("firstname")
	private String firstName;

	@JsonProperty("initials")
	private String initials;

	@JsonProperty("surname")
	private String surname;

}
