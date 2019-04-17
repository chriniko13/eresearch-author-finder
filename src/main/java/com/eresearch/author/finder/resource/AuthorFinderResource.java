package com.eresearch.author.finder.resource;

import com.eresearch.author.finder.dto.AuthorFinderImmediateResultDto;
import com.eresearch.author.finder.exception.DataValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;
import com.eresearch.author.finder.dto.TestDto;

public interface AuthorFinderResource {

	TestDto testMethod(TestDto dto);// proof of concept method.

	DeferredResult<AuthorFinderResultsDto> authorFinderOperation(AuthorFinderDto authorFinderDto);

	ResponseEntity<AuthorFinderImmediateResultDto> authorFinderNonBlockConsumption(String transactionId, AuthorFinderDto authorFinderDto) throws DataValidationException;
}
