package com.github.leoyakubov.twofactorauth.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.servlet.http.HttpServletRequest;
import com.github.leoyakubov.twofactorauth.exception.TooManyRequestsException;

import java.util.LinkedHashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("bad request on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUsernameExists(UsernameAlreadyExistsException ex,
                                                                    HttpServletRequest request) {
        log.warn("signup rejected on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailExists(EmailAlreadyExistsException ex,
                                                                 HttpServletRequest request) {
        log.warn("signup rejected on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex,
                                                              HttpServletRequest request) {
        log.warn("resource not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "We couldn't find an account with that username or email.");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException ex,
                                                                    HttpServletRequest request) {
        log.warn("authentication failed on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "We couldn't sign you in. Check your credentials and try again.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        log.warn("validation failed on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildValidationBody(ex));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyRequests(TooManyRequestsException ex,
                                                                      HttpServletRequest request) {
        log.warn("rate limit exceeded on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoResourceFound(NoResourceFoundException ex,
                                                                     HttpServletRequest request) {
        log.warn("resource not found on {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "We couldn't find that page or resource.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    private ResponseEntity<Map<String, String>> build(HttpStatus status, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> buildValidationBody(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Please review the highlighted fields and try again.");
        body.put("errors", ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        this::toFriendlyFieldMessage,
                        (first, second) -> first,
                        LinkedHashMap::new
                )));
        return body;
    }

    private String toFriendlyFieldMessage(org.springframework.validation.FieldError error) {
        List<String> codes = error.getCodes() == null ? List.of() : Arrays.asList(error.getCodes());

        if (codes.stream().anyMatch(code -> code != null && code.startsWith("NotBlank"))) {
            return switch (error.getField()) {
                case "name" -> "Please enter your name.";
                case "username" -> "Please choose a username.";
                case "email" -> "Please enter your email address.";
                case "password" -> "Please choose a password.";
                default -> "This field is required.";
            };
        }

        if (codes.stream().anyMatch(code -> code != null && code.startsWith("Email"))) {
            return "Please enter a valid email address.";
        }

        if (codes.stream().anyMatch(code -> code != null && code.startsWith("Size"))) {
            Integer max = null;
            Integer min = null;
            Object[] arguments = error.getArguments();
            if (arguments != null) {
                for (Object argument : arguments) {
                    if (argument instanceof Integer value) {
                        if (max == null) {
                            max = value;
                        } else if (min == null) {
                            min = value;
                        }
                    }
                }
            }

            if (min != null && max != null) {
                return switch (error.getField()) {
                    case "name" -> "Your name must be between " + min + " and " + max + " characters long.";
                    case "username" -> "Your username must be between " + min + " and " + max + " characters long.";
                    case "password" -> "Your password must be between " + min + " and " + max + " characters long.";
                    default -> "This field must be between " + min + " and " + max + " characters long.";
                };
            }
        }

        return error.getDefaultMessage() == null ? "Please check this field." : error.getDefaultMessage();
    }
}
