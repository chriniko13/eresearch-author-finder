package com.eresearch.author.finder.service.scopus.author.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eresearch.author.finder.design.DesignPattern;
import com.eresearch.author.finder.design.DesingPatterns;
import com.eresearch.author.finder.dto.AuthorNameDto;

@DesignPattern(DesingPatterns.STRATEGY)
@Component
public class FirstnameAndLastnameSearch implements SearchQuery {

	@Value("${scopus.author.search.and.query.enabled}")
	private String applyAndSearchQuery;
	
	@Override
	public String construct(final AuthorNameDto authorName) {
		return construct(authorName, AND);
	}

	@Override
	public Boolean canConstruct() {
		return Boolean.valueOf(applyAndSearchQuery);
	}

}
