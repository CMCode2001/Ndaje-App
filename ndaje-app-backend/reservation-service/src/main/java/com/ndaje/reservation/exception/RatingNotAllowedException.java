package com.ndaje.reservation.exception;

public class RatingNotAllowedException extends RuntimeException {
    public RatingNotAllowedException(String message) {
        super(message);
    }
}
