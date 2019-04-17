package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewAffiliationCurrent {

	@JsonProperty("affiliation-url")
	private String url;

	@JsonProperty("affiliation-id")
	private String id;

	@JsonProperty("affiliation-name")
	private String name;

	@JsonProperty("affiliation-city")
	private String city;

	@JsonProperty("affiliation-country")
	private String country;

}
