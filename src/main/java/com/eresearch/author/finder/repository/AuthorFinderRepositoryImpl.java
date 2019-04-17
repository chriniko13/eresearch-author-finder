package com.eresearch.author.finder.repository;


import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.codahale.metrics.Timer;
import com.eresearch.author.finder.dao.AuthorFinderDao;
import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorFinderResultsDto;
import com.eresearch.author.finder.dto.AuthorSearchViewDto;
import com.eresearch.author.finder.dto.AuthorSearchViewResultsDto;
import com.eresearch.author.finder.metrics.entries.RepositoryLayerMetricEntry;
import com.eresearch.author.finder.repository.extractor.LinkExtractor;
import com.eresearch.author.finder.repository.extractor.LinkExtractorRefIdentifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j;

@Log4j
@Repository
public class AuthorFinderRepositoryImpl implements AuthorFinderRepository {

    @Autowired
    private Clock clock;

    @Qualifier("elsevierObjectMapper")
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("dbOperationsExecutor")
    private ExecutorService dbOperationsExecutor;

    @Autowired
    private RepositoryLayerMetricEntry repositoryLayerMetricEntry;

    @Autowired
    private AuthorFinderDao authorFinderDao;

    @Autowired
    private LinkExtractor linkExtractor;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Qualifier("transactionTemplate")
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public void save(AuthorFinderDto authorFinderDto, AuthorFinderResultsDto authorFinderResultsDto) {

        Runnable task = saveTask(authorFinderDto, authorFinderResultsDto);
        CompletableFuture.runAsync(task, dbOperationsExecutor);

    }

    private Runnable saveTask(final AuthorFinderDto authorFinderDto, final AuthorFinderResultsDto authorFinderResultsDto) {
        return () -> {

            Timer.Context context = repositoryLayerMetricEntry.getRepositoryLayerTimer().time();
            try {

                final String sql = authorFinderDao.getInsertQueryForSearchResultsTable();

                final ArrayList<AuthorSearchViewResultsDto> resultsToStore = new ArrayList<>(authorFinderResultsDto.getResults());

                final String authorName = objectMapper.writeValueAsString(authorFinderDto);

                final Timestamp creationTimestamp = Timestamp.from(Instant.now(clock));

                this.executeSaveStatements(sql, resultsToStore, authorName, creationTimestamp);

                log.info("AuthorFinderRepositoryImpl#save --- operation completed successfully.");

            } catch (JsonProcessingException e) {

                log.error("AuthorFinderRepositoryImpl#save --- error occurred --- not even tx initialized.", e);

            } finally {
                context.stop();
            }
        };
    }


    private void executeSaveStatements(final String sql,
                                       final ArrayList<AuthorSearchViewResultsDto> resultsToStore,
                                       final String authorName,
                                       final Timestamp creationTimestamp) {

        for (AuthorSearchViewResultsDto authorSearchViewResultsDto : resultsToStore) {
            executeSaveStatement(sql, authorName, creationTimestamp, authorSearchViewResultsDto);
        }
    }

    private void executeSaveStatement(final String sql,
                                      final String authorName,
                                      final Timestamp creationTimestamp,
                                      final AuthorSearchViewResultsDto authorSearchViewResultsDto) {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus txStatus) {

                try {
                    List<String> linksConsumedPerEntry = linkExtractor.extractLinksConsumedFromElsevierApi(authorSearchViewResultsDto,
                            LinkExtractorRefIdentifier.SELF);
                    String linksConsumedPerEntryStr = objectMapper.writeValueAsString(linksConsumedPerEntry);

                    String firstLink = linkExtractor.extractLinkConsumedFromElsevierApi(authorSearchViewResultsDto,
                            LinkExtractorRefIdentifier.FIRST);

                    String lastLink = linkExtractor.extractLinkConsumedFromElsevierApi(authorSearchViewResultsDto,
                            LinkExtractorRefIdentifier.LAST);

                    AuthorSearchViewDto authorSearchViewDto = authorSearchViewResultsDto.getAuthorSearchViewDto();
                    String authorSearchViewDtoStr = objectMapper.writeValueAsString(authorSearchViewDto);

                    jdbcTemplate.update(sql,
                            authorName,
                            authorSearchViewDtoStr,
                            linksConsumedPerEntryStr,
                            firstLink,
                            lastLink,
                            creationTimestamp);

                } catch (DataAccessException | JsonProcessingException e) {

                    log.error("AuthorFinderRepositoryImpl#save --- error occurred --- proceeding with rollback.", e);
                    txStatus.setRollbackOnly();

                }
            }
        });
    }
}
