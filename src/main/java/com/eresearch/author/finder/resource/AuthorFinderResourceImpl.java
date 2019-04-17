package com.eresearch.author.finder.resource;

import com.codahale.metrics.Timer;
import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderImmediateResultDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;
import com.eresearch.author.finder.dto.TestDto;
import com.eresearch.author.finder.exception.BusinessProcessingException;
import com.eresearch.author.finder.exception.DataValidationException;
import com.eresearch.author.finder.metrics.entries.ResourceLayerMetricEntry;
import com.eresearch.author.finder.service.AuthorFinderService;
import com.eresearch.author.finder.validator.Validator;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/author-finder")
@Log4j
public class AuthorFinderResourceImpl implements AuthorFinderResource {

    private static final Long DEFERRED_RESULT_TIMEOUT = TimeUnit.MILLISECONDS.toMinutes(7);

    @Autowired
    private AuthorFinderService authorFinderService;

    @Autowired
    private Validator<AuthorFinderDto> authorFinderDtoValidator;

    @Qualifier("scopusAuthorSearchExecutor")
    @Autowired
    private ExecutorService scopusAuthorSearchExecutor;

    @Autowired
    private ResourceLayerMetricEntry resourceLayerMetricEntry;

    @RequestMapping(method = RequestMethod.POST, path = "/test", consumes = {
            MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @Override
    public @ResponseBody
    TestDto testMethod(@RequestBody TestDto testDto) {
        try {

            log.info("---testMethod called---");
            return authorFinderService.testMethod(testDto);

        } catch (Exception ex) { // so general exception should be avoided
            // (proof of concept method).
            log.error("---error occurred in testMethod---", ex);
            throw ex;
        }
    }

    @RequestMapping(method = RequestMethod.POST, path = "/find", consumes = {
            MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @Override
    public @ResponseBody
    DeferredResult<AuthorFinderResultsDto> authorFinderOperation(
            @RequestBody AuthorFinderDto authorFinderDto) {

        DeferredResult<AuthorFinderResultsDto> deferredResult = new DeferredResult<>(DEFERRED_RESULT_TIMEOUT);

        Runnable task = authorFinderOperation(authorFinderDto, deferredResult);
        scopusAuthorSearchExecutor.execute(task);

        return deferredResult;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/find-q", consumes = {
            MediaType.APPLICATION_JSON_UTF8_VALUE}, produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    @Override
    public @ResponseBody
    ResponseEntity<AuthorFinderImmediateResultDto> authorFinderNonBlockConsumption(
            @RequestHeader(value = "Transaction-Id") String transactionId,
            @RequestBody AuthorFinderDto authorFinderDto) throws DataValidationException {

        authorFinderDtoValidator.validate(authorFinderDto);

        Runnable task = () -> authorFinderService.authorFinderNonBlockConsumption(transactionId, authorFinderDto);
        scopusAuthorSearchExecutor.execute(task);

        return ResponseEntity.ok(new AuthorFinderImmediateResultDto("Response will be written in queue."));
    }

    private Runnable authorFinderOperation(AuthorFinderDto authorFinderDto, DeferredResult<AuthorFinderResultsDto> deferredResult) {

        return () -> {

            final Timer.Context context = resourceLayerMetricEntry.getResourceLayerTimer().time();
            try {

                authorFinderDtoValidator.validate(authorFinderDto);

                AuthorFinderResultsDto authorFinderResultsDto = authorFinderService.authorFinderOperation(authorFinderDto);

                resourceLayerMetricEntry.getSuccessResourceLayerCounter().inc();

                deferredResult.setResult(authorFinderResultsDto);

            } catch (DataValidationException | BusinessProcessingException e) {

                log.error("AuthorFinderResourceImpl#authorFinderOperation --- error occurred.", e);

                resourceLayerMetricEntry.getFailureResourceLayerCounter().inc();

                deferredResult.setErrorResult(e);

            } finally {
                context.stop();
            }

        };
    }

}
