package com.eresearch.author.finder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthorFinderQueueResultDto {

    private String transactionId;
    private String exceptionMessage;
    private AuthorFinderResultsDto authorFinderResultsDto;
}
