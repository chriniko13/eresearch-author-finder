package com.eresearch.author.finder.service;

import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;
import com.eresearch.author.finder.dto.TestDto;
import com.eresearch.author.finder.exception.BusinessProcessingException;

public interface AuthorFinderService {

    TestDto testMethod(TestDto testDto);

    AuthorFinderResultsDto authorFinderOperation(AuthorFinderDto authorFinderDto) throws BusinessProcessingException;

    void authorFinderNonBlockConsumption(String transactionId, AuthorFinderDto authorFinderDto);
}
