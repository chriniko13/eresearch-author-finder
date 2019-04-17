package com.eresearch.author.finder.exception;

import com.eresearch.author.finder.error.EresearchAuthorFinderError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class DataValidationException extends Exception {

    private static final long serialVersionUID = -4807690929767265288L;

    private final EresearchAuthorFinderError eresearchAuthorFinderError;

    public DataValidationException(EresearchAuthorFinderError eresearchAuthorFinderError, String message) {
        super(message);
        this.eresearchAuthorFinderError = eresearchAuthorFinderError;
    }

    public EresearchAuthorFinderError getEresearchAuthorFinderError() {
        return eresearchAuthorFinderError;
    }
}
