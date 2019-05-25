package com.eresearch.author.finder.service;

import com.codahale.metrics.Timer;
import com.eresearch.author.finder.application.configuration.JmsConfiguration;
import com.eresearch.author.finder.connector.ScopusSearchConnector;
import com.eresearch.author.finder.dto.*;
import com.eresearch.author.finder.exception.BusinessProcessingException;
import com.eresearch.author.finder.metrics.entries.ServiceLayerMetricEntry;
import com.eresearch.author.finder.service.scopus.author.search.SearchQuery;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Log4j
public class AuthorFinderServiceImpl implements AuthorFinderService {

    @Value("${test.apikey.consumer}")
    private String apiKey;

    @Autowired
    private List<SearchQuery> searchQueryStrategies;

    @Autowired
    private ScopusSearchConnector scopusSearchConnector;

    @Autowired
    private Clock clock;

    @Autowired
    private ServiceLayerMetricEntry serviceLayerMetricEntry;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    @Qualifier("elsevierObjectMapper")
    private ObjectMapper objectMapper;

    @Override
    public TestDto testMethod(TestDto testDto) {
        testDto.setOutgoingValue("TEST PASSED! apiKey = " + apiKey);
        return testDto;
    }

    @Override
    public AuthorFinderResultsDto authorFinderOperation(AuthorFinderDto authorFinderDto) throws BusinessProcessingException {

        Timer.Context context = serviceLayerMetricEntry.getServiceLayerTimer().time();
        try {
            AuthorFinderResultsDto result = new AuthorFinderResultsDto();

            AuthorNameDto authorNameDto = authorFinderDto.getAuthorName();
            List<String> authorNameQueriesToExecute = createSearchQueriesFromAuthorName().apply(authorNameDto);

            List<AuthorSearchViewResultsDto> authorToSearchResults = new ArrayList<>();
            for (String query : authorNameQueriesToExecute) {
                authorToSearchResults.addAll(searchQueryToSearchResult(query));
            }

            result.setRequestedAuthorFinderDto(authorFinderDto);
            result.setResultsSize(authorToSearchResults.size());
            result.setResults(authorToSearchResults);
            result.setOperationResult(Boolean.TRUE);
            result.setProcessFinishedDate(Instant.now(clock));

            return result;

        } catch (BusinessProcessingException ex) {
            log.error("AuthorFinderServiceImpl#authorFinderOperation --- error occurred.", ex);
            throw ex;
        } finally {
            context.stop();
        }
    }

    @Override
    public void authorFinderNonBlockConsumption(String transactionId, AuthorFinderDto authorFinderDto) {

        AuthorFinderQueueResultDto authorFinderQueueResultDto = null;
        try {

            AuthorFinderResultsDto authorFinderResultsDto
                    = this.authorFinderOperation(authorFinderDto);

            authorFinderQueueResultDto = new AuthorFinderQueueResultDto(transactionId, null, authorFinderResultsDto);

        } catch (BusinessProcessingException e) {

            log.error("AuthorFinderServiceImpl#authorFinderNonBlockConsumption --- error occurred.", e);

            AuthorFinderResultsDto authorFinderResultsDto = new AuthorFinderResultsDto();
            authorFinderResultsDto.setOperationResult(false); //Note: VERY IMPORTANT!!!

            authorFinderQueueResultDto = new AuthorFinderQueueResultDto(transactionId, e.toString(), authorFinderResultsDto);

        } finally {

            try {
                String resultForQueue = objectMapper.writeValueAsString(authorFinderQueueResultDto);
                jmsTemplate.convertAndSend(JmsConfiguration.QUEUES.AUTHOR_RESULTS_QUEUE, resultForQueue);
            } catch (JsonProcessingException e) {
                //we can't do much things for the moment here...
                log.error("AuthorFinderServiceImpl#authorFinderNonBlockConsumption --- error occurred.", e);
            }

        }
    }

    private List<AuthorSearchViewResultsDto> searchQueryToSearchResult(String query) throws BusinessProcessingException {
        return scopusSearchConnector.searchAuthorExhaustive(query);
    }

    private Function<AuthorNameDto, List<String>> createSearchQueriesFromAuthorName() {
        return authorName -> searchQueryStrategies.stream()
                .filter(SearchQuery::canConstruct)
                .map(searchQueryStrategy -> searchQueryStrategy.construct(authorName))
                .collect(Collectors.toList());
    }

}
