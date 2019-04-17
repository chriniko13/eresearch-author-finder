package com.eresearch.author.finder.service.scopus.author.search;

import java.util.Optional;

import com.eresearch.author.finder.design.DesignPattern;
import com.eresearch.author.finder.design.DesingPatterns;
import com.eresearch.author.finder.dto.AuthorNameDto;

@DesignPattern(DesingPatterns.STRATEGY)
public interface SearchQuery {

	String OR = "or";
	String AND = "and";
	
	String AUTHLAST = "authlast"; // contains surname, eg:
														// Christidis.

  	String AUTHFIRST = "authfirst"; // contains firstname
														// and (initials)[0-1],
														// eg: Nikolaos T.

	String NULL_NORMALIZER = "";
	String EMPTY = " ";

	Boolean canConstruct();
	
	String construct(final AuthorNameDto authoName);
	
	default String construct(final AuthorNameDto authorName, final String booleanOperator) {
		
		String normalizedFirstName = Optional.ofNullable(authorName)
										     .map(AuthorNameDto::getFirstName)
										     .orElse(NULL_NORMALIZER);

		String normalizedInitials = Optional.ofNullable(authorName)
										    .map(AuthorNameDto::getInitials)
										    .map(initials -> EMPTY + initials)
										    .orElse(NULL_NORMALIZER);

		String normalizedSurname = Optional.ofNullable(authorName)
										   .map(AuthorNameDto::getSurname)
										   .orElse(NULL_NORMALIZER);

		StringBuilder sb = new StringBuilder();
		sb.append(AUTHLAST)
		  .append("(")
		  .append(normalizedSurname)
		  .append(")")
		  .append(EMPTY)
		  .append(booleanOperator)
		  .append(EMPTY)
		  .append(AUTHFIRST)
		  .append("(")
		  .append(normalizedFirstName)
		  .append(normalizedInitials)
		  .append(")");

		return sb.toString();
	}
}
