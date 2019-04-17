package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuthorFinderDto {

	@JsonProperty("author-name")
	private AuthorNameDto authorName;

}
