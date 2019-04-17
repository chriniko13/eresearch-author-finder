package com.eresearch.author.finder.validator;

import com.eresearch.author.finder.dto.AuthorFinderDto;
import com.eresearch.author.finder.dto.AuthorNameDto;
import com.eresearch.author.finder.error.EresearchAuthorFinderError;
import com.eresearch.author.finder.exception.DataValidationException;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/*
NOTE: only initials can be null or empty.
 */
@Component
@Log4j
public class AuthorFinderDtoValidator implements Validator<AuthorFinderDto> {

    @Override
    public void validate(AuthorFinderDto authorFinderDto) throws DataValidationException {

        //first validation...
        if (Objects.isNull(authorFinderDto)) {
            log.error("AuthorFinderDtoValidator#validate --- error occurred (first validation) --- authorFinderDto = " + authorFinderDto);
            throw new DataValidationException(EresearchAuthorFinderError.DATA_VALIDATION_ERROR, EresearchAuthorFinderError.DATA_VALIDATION_ERROR.getMessage());
        }

        //second validation...
        if (Objects.isNull(authorFinderDto.getAuthorName())) {
            log.error("AuthorFinderDtoValidator#validate --- error occurred (second validation) --- authorFinderDto = " + authorFinderDto);
            throw new DataValidationException(EresearchAuthorFinderError.DATA_VALIDATION_ERROR, EresearchAuthorFinderError.DATA_VALIDATION_ERROR.getMessage());
        }

        //third validation...
        validateAuthorNameDto(authorFinderDto.getAuthorName());
    }

    private void validateAuthorNameDto(AuthorNameDto authorNameDto) throws DataValidationException {

        if (orReducer(
                isNull(authorNameDto.getFirstName()),
                isNull(authorNameDto.getSurname()),
                isEmpty(authorNameDto.getFirstName()),
                isEmpty(authorNameDto.getSurname()))) {
            log.error("AuthorFinderDtoValidator#validate --- error occurred (third validation) --- authorFinderDto = " + authorNameDto);
            throw new DataValidationException(EresearchAuthorFinderError.DATA_VALIDATION_ERROR, EresearchAuthorFinderError.DATA_VALIDATION_ERROR.getMessage());
        }
    }

    private Boolean orReducer(Boolean... booleans) {
        return Arrays.stream(booleans).reduce(false, (acc, elem) -> acc || elem);
    }

    private Boolean isEmpty(String datum) {
        return "".equals(datum);
    }

    private <T> Boolean isNull(T datum) {
        return Objects.isNull(datum);
    }

}
