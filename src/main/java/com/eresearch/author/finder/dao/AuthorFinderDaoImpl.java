package com.eresearch.author.finder.dao;

import org.springframework.stereotype.Component;

@Component
public class AuthorFinderDaoImpl implements AuthorFinderDao {

    @Override
    public String getInsertQueryForSearchResultsTable() {
        return "INSERT author_finder_consumer.search_results(author_name, author_results, links_consumed, first_link, last_link, creation_timestamp) VALUES (?, ?, ?, ?, ?, ?)";
    }

    @Override
    public String getDeleteQueryForSearchResultsTable() {
        return "DELETE FROM author_finder_consumer.search_results";
    }

    @Override
    public String getResetAutoIncrementForSearchResultsTable() {
        return "ALTER TABLE author_finder_consumer.search_results AUTO_INCREMENT = 1";
    }

    @Override
    public String getSelectQueryForSearchResultsTable() {
        return "SELECT * FROM author_finder_consumer.search_results";
    }

    @Override
    public String getCreationQueryForSearchResultsTable() {
        return "CREATE TABLE IF NOT EXISTS author_finder_consumer.search_results(id BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT, author_name VARCHAR(255) DEFAULT NULL, author_results LONGTEXT, links_consumed MEDIUMTEXT, first_link MEDIUMTEXT, last_link MEDIUMTEXT, creation_timestamp TIMESTAMP NULL DEFAULT NULL, PRIMARY KEY (id), KEY author_name_idx (author_name) ) ENGINE=InnoDB DEFAULT CHARSET=utf8";
    }

    @Override
    public String getDropQueryForSearchResultsTable() {
        return "DROP TABLE IF EXISTS author_finder_consumer.search_results";
    }
}
