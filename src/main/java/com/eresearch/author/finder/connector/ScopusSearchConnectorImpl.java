package com.eresearch.author.finder.connector;

import com.codahale.metrics.Timer;
import com.eresearch.author.finder.connector.communicator.Communicator;
import com.eresearch.author.finder.connector.guard.NoResultsAvailableGuard;
import com.eresearch.author.finder.connector.guard.UniqueEntriesGuard;
import com.eresearch.author.finder.connector.pagination.ScopusSearchPaginationResourcesCalculator;
import com.eresearch.author.finder.dto.AuthorSearchViewLink;
import com.eresearch.author.finder.dto.AuthorSearchViewResultsDto;
import com.eresearch.author.finder.error.EresearchAuthorFinderError;
import com.eresearch.author.finder.exception.BusinessProcessingException;
import com.eresearch.author.finder.metrics.entries.ConnectorLayerMetricEntry;
import com.eresearch.author.finder.worker.WorkerDispatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Log4j
public class ScopusSearchConnectorImpl implements ScopusSearchConnector {

    private static final String API_KEY = "apikey";
    private static final String QUERY = "query";

    private static final Integer DEFAULT_RESOURCE_PAGE_COUNT = 25;
    private static final String START_QUERY_PARAM = "start";
    private static final String COUNT_QUERY_PARAM = "count";

    private static final String FIRST_LINK_REF_VALUE = "first";
    private static final String LAST_LINK_REF_VALUE = "last";

    @Value("${scopus.apikey.consumer}")
    private String apiKey;

    @Value("${scopus.author-search.url}")
    private String scopusAuthorSearchUrl;

    @Qualifier("elsevierObjectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScopusSearchPaginationResourcesCalculator scopusSearchPaginationResourcesCalculator;

    @Autowired
    private WorkerDispatcher workerDispatcher;

    @Autowired
    @Qualifier("basicCommunicator")
    private Communicator communicator;

    @Autowired
    private ConnectorLayerMetricEntry connectorLayerMetricEntry;

    @Autowired
    private UniqueEntriesGuard uniqueEntriesGuard;

    @Autowired
    private NoResultsAvailableGuard noResultsAvailableGuard;

    @Override
    public List<AuthorSearchViewResultsDto> searchAuthorExhaustive(String query) throws BusinessProcessingException {

        Timer.Context context = connectorLayerMetricEntry.getConnectorLayerTimer().time();
        try {

            List<AuthorSearchViewResultsDto> results = new ArrayList<>();

            URI uri = constructUri(query);

            AuthorSearchViewResultsDto firstResult = pullAndFetchInfo(uri);
            results.add(firstResult);

            //check if we have results for the provided query...
            if (noResultsAvailableGuard.test(firstResult)) return Collections.emptyList();

            List<URI> allResourcesToHit = calculateAllResources(firstResult, query);

            if (workerDispatcher.shouldDispatch(allResourcesToHit.size())) { //if we need to split the load then...

                results.addAll(workerDispatcher.performTask(allResourcesToHit, extractInfoWithProvidedResource()));

            } else {

                for (URI resourceToHit : allResourcesToHit) {
                    AuthorSearchViewResultsDto result = pullAndFetchInfo(resourceToHit);
                    results.add(result);
                }

            }

            uniqueEntriesGuard.apply(results);
            return results;

        } catch (BusinessProcessingException e) {

            log.error("ScopusSearchConnectorImpl#searchAuthorExhaustive --- error occurred.", e);
            throw e;

        } catch (RestClientException | IOException e) {

            log.error("ScopusSearchConnectorImpl#searchAuthorExhaustive --- error occurred.", e);

            throw new BusinessProcessingException(
                    EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR,
                    EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR.getMessage(),
                    e);

        } catch (CompletionException e) {

            log.error("ScopusSearchConnectorImpl#searchAuthorExhaustive --- error occurred.",
                    e.getCause());

            throw new BusinessProcessingException(
                    EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR,
                    EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR.getMessage(),
                    e.getCause());

        } finally {
            context.stop();
        }

    }

    private Function<URI, AuthorSearchViewResultsDto> extractInfoWithProvidedResource() {
        return resource -> {
            try {
                return pullAndFetchInfo(resource);
            } catch (IOException e) {
                throw new CompletionException(e);
            }
        };
    }

    private List<URI> calculateAllResources(AuthorSearchViewResultsDto firstResult, String query) throws BusinessProcessingException {

        try {
            // Note: if we do not have a first resource page, we get out of here!
            Optional<AuthorSearchViewLink> firstResourcePage = extractFirstResourcePage(firstResult);
            if (!firstResourcePage.isPresent()) {
                return Collections.emptyList();
            }

            // Note: if we do not have a last resource page, we get out of here!
            Optional<AuthorSearchViewLink> lastResourcePage = extractLastResourcePageLink(firstResult);
            if (!lastResourcePage.isPresent()) {
                return Collections.emptyList();
            }

            List<String> startPageQueryParamsOfAllResources
                    = scopusSearchPaginationResourcesCalculator.calculateStartPageQueryParams(
                    firstResourcePage.get().getHref(),
                    lastResourcePage.get().getHref(),
                    DEFAULT_RESOURCE_PAGE_COUNT);

            return startPageQueryParamsOfAllResources.stream()
                    .map(startPageQueryParam -> constructUri(query,
                            startPageQueryParam,
                            String.valueOf(DEFAULT_RESOURCE_PAGE_COUNT)))
                    .collect(Collectors.toList());

        } catch (BusinessProcessingException ex) {
            log.error("ScopusSearchConnectorImpl#calculateAllResources --- error occurred.", ex);
            throw ex;
        }

    }

    private Optional<AuthorSearchViewLink> extractLastResourcePageLink(AuthorSearchViewResultsDto firstResult) {
        return firstResult
                .getAuthorSearchViewDto()
                .getLinks()
                .stream()
                .filter(link -> link.getRef().contains(LAST_LINK_REF_VALUE))
                .findAny();
    }

    private Optional<AuthorSearchViewLink> extractFirstResourcePage(AuthorSearchViewResultsDto firstResult) throws BusinessProcessingException {

        return firstResult
                .getAuthorSearchViewDto()
                .getLinks()
                .stream()
                .filter(link -> link.getRef().contains(FIRST_LINK_REF_VALUE))
                .findAny();
    }

    private URI constructUri(String query, String startQueryParam, String countQueryParam) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(scopusAuthorSearchUrl);
        builder.queryParam(API_KEY, apiKey);
        builder.queryParam(QUERY, query);
        builder.queryParam(START_QUERY_PARAM, startQueryParam);
        builder.queryParam(COUNT_QUERY_PARAM, countQueryParam);

        return builder.build().toUri();
    }

    private URI constructUri(String query) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(scopusAuthorSearchUrl);
        builder.queryParam(API_KEY, apiKey);
        builder.queryParam(QUERY, query);

        return builder.build().toUri();
    }

    private AuthorSearchViewResultsDto pullAndFetchInfo(URI uri) throws IOException {
        String resultInString = communicator.communicateWithElsevier(uri);
        return objectMapper.readValue(resultInString, AuthorSearchViewResultsDto.class);
    }
}
