package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewNameVariant {

	@JsonProperty("@_fa")
	private String fa;

	@JsonProperty("surname")
	private String surname;

	@JsonProperty("given-name")
	private String givenName;

	@JsonProperty("initials")
	private String initials;

}
