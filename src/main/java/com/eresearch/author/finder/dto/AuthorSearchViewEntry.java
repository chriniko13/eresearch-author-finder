package com.eresearch.author.finder.dto;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EqualsAndHashCode(of = {"dcIdentifier"})
@Getter
@Setter
@NoArgsConstructor
public class AuthorSearchViewEntry {

	@JsonProperty("@force-array")
	private String forceArray;

	@JsonProperty("error")
	private String error;

	@JsonProperty("@_fa")
	private String fa;

	@JsonProperty("link")
	private Collection<AuthorSearchViewLink> links;

	@JsonProperty("prism:url")
	private String prismUrl;

	@JsonProperty("dc:identifier")
	private String dcIdentifier;

	@JsonProperty("eid")
	private String eid;

	@JsonProperty("orcid")
	private String orcId;

	@JsonProperty("preferred-name")
	private AuthorSearchViewPreferredName preferredName;

	@JsonProperty("name-variant")
	private Collection<AuthorSearchViewNameVariant> nameVariants;

	@JsonProperty("document-count")
	private String documentCount;

	@JsonProperty("subject-area")
	private Collection<AuthorSearchViewSubjectArea> subjectAreas;

	@JsonProperty("affiliation-current")
	private AuthorSearchViewAffiliationCurrent affiliationCurrent;

}
