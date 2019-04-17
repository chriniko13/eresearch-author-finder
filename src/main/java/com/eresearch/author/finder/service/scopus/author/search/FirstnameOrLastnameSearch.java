package com.eresearch.author.finder.service.scopus.author.search;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.eresearch.author.finder.design.DesignPattern;
import com.eresearch.author.finder.design.DesingPatterns;
import com.eresearch.author.finder.dto.AuthorNameDto;

@DesignPattern(DesingPatterns.STRATEGY)
@Component
public class FirstnameOrLastnameSearch implements SearchQuery {

	@Value("${scopus.author.search.or.query.enabled}")
	private String applyOrSearchQuery;

	@Override
	public String construct(final AuthorNameDto authorName) {
		return construct(authorName, OR);
	}

	@Override
	public Boolean canConstruct() {
		return Boolean.valueOf(applyOrSearchQuery);
	}

}
