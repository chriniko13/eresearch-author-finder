package com.eresearch.author.finder.connector.guard;

import java.util.Collection;

import com.eresearch.author.finder.dto.AuthorSearchViewEntry;
import com.eresearch.author.finder.dto.AuthorSearchViewQuery;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode(of = {"totalUniqueResults", "authorSearchViewQuery"})
@Getter
@Setter
class MemoGuard {

    private Integer totalUniqueResults;
    private AuthorSearchViewQuery authorSearchViewQuery;
    private Collection<AuthorSearchViewEntry> authorSearchViewEntries;

    MemoGuard(Integer totalUniqueResults, AuthorSearchViewQuery authorSearchViewQuery, Collection<AuthorSearchViewEntry> authorSearchViewEntries) {
        this.totalUniqueResults = totalUniqueResults;
        this.authorSearchViewQuery = authorSearchViewQuery;
        this.authorSearchViewEntries = authorSearchViewEntries;
    }
}