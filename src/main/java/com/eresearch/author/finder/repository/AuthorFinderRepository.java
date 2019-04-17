package com.eresearch.author.finder.repository;

import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;

public interface AuthorFinderRepository {

    void save(AuthorFinderDto authorFinderDto, AuthorFinderResultsDto authorFinderResultsDto);
}
