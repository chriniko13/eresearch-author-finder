package com.eresearch.author.finder.dto;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewDto {

	@JsonProperty("opensearch:totalResults")
	private String totalResults;

	@JsonProperty("opensearch:startIndex")
	private String startIndex;

	@JsonProperty("opensearch:itemsPerPage")
	private String itemsPerPage;

	@JsonProperty("opensearch:Query")
	private AuthorSearchViewQuery query;

	@JsonProperty("link")
	private Collection<AuthorSearchViewLink> links;

	@JsonProperty("entry")
	private Collection<AuthorSearchViewEntry> entries;

}
