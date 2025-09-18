package com.challenge.todo.web;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the todo application.
 * Provides consistent error responses across all controllers.
 */
@ControllerAdvice
public class ErrorHandler {

  private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

  /**
   * Handles IllegalArgumentException with smart status code detection.
   * Maps different error types to appropriate HTTP status codes based on message content.
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
      IllegalArgumentException ex, HttpServletRequest request) {
    
    String message = ex.getMessage();
    HttpStatus status = determineStatusFromMessage(message);
    
    logger.warn("Validation error: {} (Status: {})", message, status.value());
    
    return ResponseEntity.status(status)
        .body(createErrorResponse(status, message, request.getRequestURI()));
  }

  /**
   * Handles validation errors from @Valid annotations.
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    
    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });

    String message = "Validation failed for " + validationErrors.size() + " field(s)";
    logger.warn("Validation error: {}", validationErrors);

    Map<String, Object> errorResponse = createErrorResponse(
        HttpStatus.BAD_REQUEST, message, request.getRequestURI());
    errorResponse.put("validationErrors", validationErrors);

    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * Handles HTTP method not supported errors.
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotSupportedException(
      HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
    
    String message = String.format("Method '%s' not supported for this endpoint. Supported methods: %s",
        ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
    
    logger.warn("Method not supported: {}", message);
    
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(createErrorResponse(HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI()));
  }

  /**
   * Handles unsupported media type errors.
   */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupportedException(
      HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
    
    String message = String.format("Media type '%s' not supported. Supported types: %s",
        ex.getContentType(), ex.getSupportedMediaTypes());
    
    logger.warn("Media type not supported: {}", message);
    
    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
        .body(createErrorResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request.getRequestURI()));
  }

  /**
   * Handles missing request parameter errors.
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParameterException(
      MissingServletRequestParameterException ex, HttpServletRequest request) {
    
    String message = String.format("Required parameter '%s' of type '%s' is missing",
        ex.getParameterName(), ex.getParameterType());
    
    logger.warn("Missing parameter: {}", message);
    
    return ResponseEntity.badRequest()
        .body(createErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
  }

  /**
   * Handles malformed JSON request body errors.
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleMessageNotReadableException(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    
    String message = "Malformed JSON request body";
    logger.warn("Message not readable: {}", ex.getMessage());
    
    return ResponseEntity.badRequest()
        .body(createErrorResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI()));
  }

  /**
   * Handles generic runtime exceptions.
   */
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {
    
    logger.error("Runtime exception occurred: {}", ex.getMessage(), ex);
    
    // Don't expose internal error details to clients
    String message = "An unexpected error occurred";
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI()));
  }

  /**
   * Handles any other unhandled exceptions.
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleGenericException(
      Exception ex, HttpServletRequest request) {
    
    logger.error("Unhandled exception occurred: {}", ex.getMessage(), ex);
    
    // Don't expose internal error details to clients
    String message = "An unexpected error occurred";
    
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request.getRequestURI()));
  }

  /**
   * Determines the appropriate HTTP status code based on the error message content.
   */
  private HttpStatus determineStatusFromMessage(String message) {
    if (message == null) {
      return HttpStatus.BAD_REQUEST;
    }

    String lowerMessage = message.toLowerCase();
    
    // Check for "not found" patterns
    if (lowerMessage.contains("not found") || lowerMessage.contains("does not exist")) {
      return HttpStatus.NOT_FOUND;
    }
    
    // Check for "already exists" or duplicate patterns
    if (lowerMessage.contains("already exists") || lowerMessage.contains("duplicate")) {
      return HttpStatus.CONFLICT;
    }
    
    // Default to bad request for other validation errors
    return HttpStatus.BAD_REQUEST;
  }

  /**
   * Creates a standardized error response map.
   */
  private Map<String, Object> createErrorResponse(HttpStatus status, String message, String path) {
    Map<String, Object> errorResponse = new HashMap<>();
    errorResponse.put("timestamp", Instant.now());
    errorResponse.put("status", status.value());
    errorResponse.put("error", status.getReasonPhrase());
    errorResponse.put("message", message);
    errorResponse.put("path", path);
    return errorResponse;
  }
}
