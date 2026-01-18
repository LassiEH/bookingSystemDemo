package org.example.bookingsystemdemo.Exception;

import org.example.bookingsystemdemo.Models.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ApiError> handleInvalidBooking(InvalidBookingException e) {
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(BookingConflictException.class)
    public ResponseEntity<ApiError> handleConflict(BookingConflictException e) {
        ApiError error = new ApiError(HttpStatus.CONFLICT, e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiError> handleNonExistentBooking(BookingNotFoundException e) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiError> handleRoomNotFound(RoomNotFoundException e) {
        ApiError error = new ApiError(HttpStatus.NOT_FOUND, e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // Napataan myös muut odottamattomat virheet, jotta nekin tulevat ApiError-muodossa
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralError(Exception e) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Odottamaton virhe tapahtui.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
