package com.juviai.user.web;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @SuppressWarnings("null")
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        if (status.is5xxServerError()) {
            log.error("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), ex.getReason(), ex);
        } else {
            log.warn("{} {} -> {} {}", request.getMethod(), request.getRequestURI(), status.value(), ex.getReason());
        }
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setTitle(status.getReasonPhrase());
        pd.setDetail(ex.getReason() != null ? ex.getReason() : status.getReasonPhrase());
        pd.setProperty("timestamp", Instant.now());
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }

        URI type = switch (status) {
            case NOT_FOUND -> URI.create("https://skillrat.errors/not-found");
            case BAD_REQUEST -> URI.create("https://skillrat.errors/bad-request");
            case UNAUTHORIZED -> URI.create("https://skillrat.errors/unauthorized");
            case FORBIDDEN -> URI.create("https://skillrat.errors/forbidden");
            case CONFLICT -> URI.create("https://skillrat.errors/conflict");
            default -> URI.create("https://skillrat.errors/internal");
        };
        pd.setType(type);
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        pd.setDetail(details);
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/validation"));
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraint(ConstraintViolationException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Constraint violation");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/constraint"));
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("{} {} -> 400 {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad request");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/bad-request"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("{} {} -> 409 {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setTitle("Conflict");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/conflict"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad request");
        String name = ex.getName();
        Object value = ex.getValue();
        pd.setDetail("Invalid value for parameter '" + name + "': " + (value != null ? value : "null"));
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/bad-request"));
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResourceFound(NoResourceFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail("Resource not found");
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/not-found"));
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("{} {} -> 404 {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setTitle("Not found");
        pd.setDetail(ex.getMessage());
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/not-found"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("{} {} -> 403 Access denied", request.getMethod(), request.getRequestURI());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setTitle("Forbidden");
        pd.setDetail("Access is denied");
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/forbidden"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(OAuth2AuthenticationException.class)
    public ProblemDetail handleOAuth2(OAuth2AuthenticationException ex, HttpServletRequest request) {
        log.warn("{} {} -> 401 {}", request.getMethod(), request.getRequestURI(), ex.getError() != null ? ex.getError().getDescription() : "Unauthorized");
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setTitle("Unauthorized");
        pd.setDetail(ex.getError().getDescription());
        pd.setProperty("timestamp", Instant.now());
        pd.setType(URI.create("https://skillrat.errors/unauthorized"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }

    @SuppressWarnings("null")
	@ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
        if (request != null) {
            log.error("{} {} -> 500 {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        } else {
            log.error("Unhandled exception -> 500 {}", ex.getMessage(), ex);
        }
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal error");
        pd.setDetail(ex.getMessage() != null ? ex.getMessage() : "Unexpected error");
        pd.setProperty("timestamp", Instant.now());
        pd.setProperty("exception", ex.getClass().getName());
        pd.setType(URI.create("https://skillrat.errors/internal"));
        if (request != null) {
            pd.setInstance(URI.create(request.getRequestURI()));
        }
        return pd;
    }
}
