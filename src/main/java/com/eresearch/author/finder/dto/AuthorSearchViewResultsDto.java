package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewResultsDto {

	@JsonProperty("search-results")
	private AuthorSearchViewDto authorSearchViewDto;

}
