package com.ndaje.trip.exception;

import com.ndaje.trip.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(TripNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleTripNotFoundException(TripNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.<Void>builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Void>builder()
                                                .success(false)
                                                .message(ex.getMessage())
                                                .build());
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException ex) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Map<String, String>>builder()
                                                .success(false)
                                                .message("Validation failed")
                                                .data(errors)
                                                .build());
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Void>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
                String message = String.format("Invalid value for parameter '%s'", ex.getName());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Void>builder()
                                                .success(false)
                                                .message(message)
                                                .build());
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(
                        ConstraintViolationException ex) {
                Map<String, String> errors = ex.getConstraintViolations().stream()
                                .collect(Collectors.toMap(
                                                violation -> violation.getPropertyPath().toString(),
                                                violation -> violation.getMessage(),
                                                (existing, replacement) -> existing));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Map<String, String>>builder()
                                                .success(false)
                                                .message("Constraint violation")
                                                .data(errors)
                                                .build());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.<Void>builder()
                                                .success(false)
                                                .message("An unexpected error occurred.")
                                                .build());
        }
}
