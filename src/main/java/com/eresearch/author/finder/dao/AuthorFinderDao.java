package com.eresearch.author.finder.dao;


public interface AuthorFinderDao {

    String getInsertQueryForSearchResultsTable();

    /*
    NOTE: this should only used for scheduler (db-cleaner).
     */
    String getDeleteQueryForSearchResultsTable();

    /*
    NOTE: this should only used for scheduler (db-cleaner).
     */
    String getResetAutoIncrementForSearchResultsTable();

    String getSelectQueryForSearchResultsTable();

    String getCreationQueryForSearchResultsTable();

    String getDropQueryForSearchResultsTable();
}
