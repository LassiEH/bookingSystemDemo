package org.example.bookingsystemdemo.Models;

import org.springframework.http.HttpStatus;

public class ApiError {
    private int status;
    private String error;
    private String message;

    public ApiError(HttpStatus status, String message) {
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }
}
