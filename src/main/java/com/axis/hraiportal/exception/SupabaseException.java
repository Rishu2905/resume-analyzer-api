package com.axis.hraiportal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;

@Getter
public class SupabaseException extends RuntimeException {

    private final HttpStatusCode status;

    public SupabaseException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

}