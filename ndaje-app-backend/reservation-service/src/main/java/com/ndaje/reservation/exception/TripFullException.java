package com.ndaje.reservation.exception;

public class TripFullException extends RuntimeException {
    public TripFullException(String message) {
        super(message);
    }
}
