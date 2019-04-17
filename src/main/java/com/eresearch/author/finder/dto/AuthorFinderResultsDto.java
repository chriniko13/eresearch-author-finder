package com.eresearch.author.finder.dto;

import java.time.Instant;
import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorFinderResultsDto {

	@JsonProperty("operation-result")
	private Boolean operationResult;
	
	@JsonProperty("process-finished-date")
	private Instant processFinishedDate;
	
	@JsonProperty("requested-author-finder-dto")
	private AuthorFinderDto requestedAuthorFinderDto;

	@JsonProperty("fetched-results-size")
	private Integer resultsSize;
	
	@JsonProperty("fetched-results")
	private Collection<AuthorSearchViewResultsDto> results;
}
