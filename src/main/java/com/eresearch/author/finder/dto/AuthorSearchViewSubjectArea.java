package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewSubjectArea {

	@JsonProperty("@abbrev")
	private String abbrev;

	@JsonProperty("@frequency")
	private String frequency;

	@JsonProperty("$")
	private String subjectArea;

}
