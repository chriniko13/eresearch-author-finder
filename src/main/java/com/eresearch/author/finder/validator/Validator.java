package com.eresearch.author.finder.validator;

import com.eresearch.author.finder.exception.DataValidationException;

public interface Validator<T> {

	void validate(T data) throws DataValidationException;
}
