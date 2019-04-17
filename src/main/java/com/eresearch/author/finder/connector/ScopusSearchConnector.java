package com.eresearch.author.finder.connector;

import com.eresearch.author.finder.dto.AuthorSearchViewResultsDto;
import com.eresearch.author.finder.exception.BusinessProcessingException;

import java.util.List;

public interface ScopusSearchConnector {

    List<AuthorSearchViewResultsDto> searchAuthorExhaustive(String query) throws BusinessProcessingException;

}
