package com.eresearch.author.finder.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TestDto {

	@JsonProperty("incoming-value")
	private String incomingValue;

	@JsonProperty("outgoing-value")
	private String outgoingValue;

}
