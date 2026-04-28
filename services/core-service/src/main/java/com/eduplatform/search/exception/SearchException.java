package com.eduplatform.search.exception;

import com.eduplatform.core.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class SearchException extends AppException {

    public SearchException(String message, String errorCode, HttpStatus httpStatus) {
        super(message, errorCode, httpStatus);
    }

    public static SearchException badRequest(String message) {
        return new SearchException(message, "SEARCH_BAD_REQUEST", HttpStatus.BAD_REQUEST);
    }
}
