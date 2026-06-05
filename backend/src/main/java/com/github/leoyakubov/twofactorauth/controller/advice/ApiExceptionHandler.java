package com.github.leoyakubov.twofactorauth.controller.advice;

import com.github.leoyakubov.twofactorauth.exception.BadRequestException;
import com.github.leoyakubov.twofactorauth.exception.EmailAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.exception.ResourceNotFoundException;
import com.github.leoyakubov.twofactorauth.exception.TooManyRequestsException;
import com.github.leoyakubov.twofactorauth.exception.UsernameAlreadyExistsException;
import com.github.leoyakubov.twofactorauth.payload.ApiErrorResponse;
import com.github.leoyakubov.twofactorauth.payload.ValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class ApiExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("bad request on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleUsernameExists(UsernameAlreadyExistsException ex,
                                                                    HttpServletRequest request) {
        log.warn("signup rejected because the username is already in use on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailExists(EmailAlreadyExistsException ex,
                                                                 HttpServletRequest request) {
        log.warn("signup rejected because the email address is already in use on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                              HttpServletRequest request) {
        log.warn("resource not found on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "We couldn't find an account with that username or email.");
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public ResponseEntity<ApiErrorResponse> handleAuthentication(AuthenticationException ex,
                                                                    HttpServletRequest request) {
        log.warn("authentication failed on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "We couldn't sign you in. Check your credentials and try again.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest request) {
        log.warn("validation failed on {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildValidationBody(ex));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<ApiErrorResponse> handleTooManyRequests(TooManyRequestsException ex,
                                                                      HttpServletRequest request) {
        log.warn("rate limit exceeded on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException ex,
                                                                     HttpServletRequest request) {
        log.warn("resource not found on {} {}", request.getMethod(), request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "We couldn't find that page or resource.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again.");
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(message));
    }

    private ValidationErrorResponse buildValidationBody(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        error -> error.getField(),
                        this::toFriendlyFieldMessage,
                        (first, second) -> first,
                        java.util.LinkedHashMap::new
                ));
        return new ValidationErrorResponse("Please review the highlighted fields and try again.", errors);
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
