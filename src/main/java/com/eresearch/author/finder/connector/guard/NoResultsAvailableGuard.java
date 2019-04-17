package com.eresearch.author.finder.connector.guard;

import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.eresearch.author.finder.dto.AuthorSearchViewEntry;
import com.eresearch.author.finder.dto.AuthorSearchViewResultsDto;

import lombok.extern.log4j.Log4j;

@Component
@Log4j
public class NoResultsAvailableGuard implements Predicate<AuthorSearchViewResultsDto> {

    @Override
    public boolean test(AuthorSearchViewResultsDto authorSearchViewResultsDto) {
        return noResultsAvailable(authorSearchViewResultsDto);
    }

    private boolean noResultsAvailable(AuthorSearchViewResultsDto authorSearchViewResultsDto) {

        final String forceArrayValueWhenNoResults = "true";
        final String errorValueWhenNoResults = "Result set was empty";

        AuthorSearchViewEntry authorSearchViewEntry = authorSearchViewResultsDto
                .getAuthorSearchViewDto()
                .getEntries()
                .stream()
                .findFirst()
                .get(); //Note: here we should not have any problem.

        if (forceArrayValueWhenNoResults.equals(authorSearchViewEntry.getForceArray())
                && errorValueWhenNoResults.equals(authorSearchViewEntry.getError())) {
            log.info("NoResultsAvailableGuard#noResultsAvailable --- no results for provided info.");
            return true;
        }
        return false;
    }
}
