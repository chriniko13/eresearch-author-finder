package com.eresearch.author.finder.connector.guard;

import com.eresearch.author.finder.dto.AuthorSearchViewDto;
import com.eresearch.author.finder.dto.AuthorSearchViewEntry;
import com.eresearch.author.finder.dto.AuthorSearchViewQuery;
import com.eresearch.author.finder.dto.AuthorSearchViewResultsDto;
import com.eresearch.author.finder.error.EresearchAuthorFinderError;
import com.eresearch.author.finder.exception.BusinessProcessingException;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Log4j
@Component
public class UniqueEntriesGuard {

    @Value("${perform.unique.entries.guard.reporting}")
    private String performUniqueEntriesGuardReporting;

    @Autowired
    private ObjectFactory<MemoGuardStack> memoGuardStackObjectFactory;

    @Autowired
    private ObjectFactory<MemoResultsReporter> memoResultsReporterObjectFactory;

    public void apply(List<AuthorSearchViewResultsDto> results) throws BusinessProcessingException {

        if (noResultsExist(results)) return;

        final MemoGuardStack memoGuardStack = memoGuardStackObjectFactory.getObject();
        final MemoResultsReporter memoResultsReporter = memoResultsReporterObjectFactory.getObject();

        for (int i = 0; i < results.size(); i++) {

            boolean isLastResult = (i == results.size() - 1);

            AuthorSearchViewResultsDto result = results.get(i);
            Integer totalUniqueResults = extractTotalUniqueResultsElsevierScopusProvides(result);
            AuthorSearchViewQuery authorSearchViewQuery = result.getAuthorSearchViewDto().getQuery();

            Optional<MemoGuard> memoPeek = memoGuardStack.peek();
            if (memoPeek.isPresent()) { //if stack is not empty.

                Collection<AuthorSearchViewEntry> notUniqueAuthorSearchViewEntries = getNotUniqueAuthorSearchViewEntries(result);
                MemoGuard memoToTestAgainst = new MemoGuard(totalUniqueResults, authorSearchViewQuery, notUniqueAuthorSearchViewEntries);

                if (memoPeek.get().equals(memoToTestAgainst)) {

                    //add the entry...
                    memoGuardStack.push(memoToTestAgainst);

                } else {
                    applyGuard(memoGuardStack, totalUniqueResults, memoResultsReporter, authorSearchViewQuery);

                    //add the new entry...
                    memoGuardStack.push(memoToTestAgainst);
                }

            } else { //if stack is empty.

                //add new entry...
                Collection<AuthorSearchViewEntry> notUniqueAuthorSearchViewEntries = getNotUniqueAuthorSearchViewEntries(result);
                MemoGuard memoGuard = new MemoGuard(totalUniqueResults, authorSearchViewQuery, notUniqueAuthorSearchViewEntries);
                memoGuardStack.push(memoGuard);
            }

            if (isLastResult) {
                applyGuard(memoGuardStack, totalUniqueResults, memoResultsReporter, authorSearchViewQuery);
            }
        }
    }

    private boolean noResultsExist(List<AuthorSearchViewResultsDto> results) {
        if (results.size() == 1) {
            AuthorSearchViewDto authorSearchViewDto = results.get(0).getAuthorSearchViewDto();

            boolean noResults = "0".equals(authorSearchViewDto.getTotalResults())
                    && "0".equals(authorSearchViewDto.getStartIndex())
                    && "0".equals(authorSearchViewDto.getItemsPerPage())
                    && authorSearchViewDto.getEntries() != null
                    && authorSearchViewDto.getEntries().size() == 1
                    && authorSearchViewDto.getEntries().iterator().next().getError().equals("Result set was empty");

            if (noResults) return true;
        }
        return false;
    }

    private void applyGuard(MemoGuardStack memoGuardStack,
                            Integer totalUniqueResults,
                            MemoResultsReporter memoResultsReporter,
                            AuthorSearchViewQuery authorSearchViewQuery) throws BusinessProcessingException {
        //time to accumulate memos...
        Integer accumulateUniqueEntriesSize = memoGuardStack.accumulateUniqueEntriesSize();

        //see if we have the proper results...
        String infoMessage = String.format("UniqueEntriesGuard#apply, accumulateUniqueEntriesSize = %s , elsevierTotalResults = %s.",
                accumulateUniqueEntriesSize, totalUniqueResults);
        log.info(infoMessage);

        if (Boolean.valueOf(performUniqueEntriesGuardReporting)) {
            memoResultsReporter.reportResults(accumulateUniqueEntriesSize, totalUniqueResults, authorSearchViewQuery);
        }

        if (!Objects.equals(accumulateUniqueEntriesSize, totalUniqueResults)) {
            throw new BusinessProcessingException(EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR, EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR.getMessage());
        }

        //clean stack...
        memoGuardStack.clean();
    }

    private Collection<AuthorSearchViewEntry> getNotUniqueAuthorSearchViewEntries(AuthorSearchViewResultsDto result) {
        return result.getAuthorSearchViewDto().getEntries();
    }

    private Integer extractTotalUniqueResultsElsevierScopusProvides(AuthorSearchViewResultsDto result) throws BusinessProcessingException {
        return Integer.valueOf(
                Optional.ofNullable(result)
                        .map(AuthorSearchViewResultsDto::getAuthorSearchViewDto)
                        .map(AuthorSearchViewDto::getTotalResults)
                        .orElseThrow(() -> new BusinessProcessingException(EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR, EresearchAuthorFinderError.BUSINESS_PROCESSING_ERROR.getMessage())));
    }


}
