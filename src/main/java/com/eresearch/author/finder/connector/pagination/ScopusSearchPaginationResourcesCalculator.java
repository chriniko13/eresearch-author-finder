package com.eresearch.author.finder.connector.pagination;

import com.eresearch.author.finder.exception.BusinessProcessingException;

import java.util.List;

public interface ScopusSearchPaginationResourcesCalculator {

    List<String> calculateStartPageQueryParams(String firstResourcePage, String lastResourcePage, Integer resourcePageCount) throws BusinessProcessingException;

}
