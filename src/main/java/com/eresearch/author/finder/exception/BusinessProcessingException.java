package com.eresearch.author.finder.exception;

import com.eresearch.author.finder.error.EresearchAuthorFinderError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class BusinessProcessingException extends Exception {

    private static final long serialVersionUID = -4352150800142237666L;

    private final EresearchAuthorFinderError eresearchAuthorFinderError;

    public BusinessProcessingException(EresearchAuthorFinderError eresearchAuthorFinderError, String message, Throwable cause) {
        super(message, cause);
        this.eresearchAuthorFinderError = eresearchAuthorFinderError;
    }

    public BusinessProcessingException(EresearchAuthorFinderError eresearchAuthorFinderError, String message) {
        super(message);
        this.eresearchAuthorFinderError = eresearchAuthorFinderError;
    }

    public EresearchAuthorFinderError getEresearchAuthorFinderError() {
        return eresearchAuthorFinderError;
    }
}
