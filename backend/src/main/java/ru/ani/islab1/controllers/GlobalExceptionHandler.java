package ru.ani.islab1.controllers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.ani.islab1.exceptions.CannotDeleteStudyGroupException;
import ru.ani.islab1.exceptions.DuplicateEntityException;
import ru.ani.islab1.exceptions.ResourceNotFoundException;
import ru.ani.islab1.services.ImportHistoryService;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final ImportHistoryService importHistoryService;
    private final String DUMMY_USER = "system-user";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArg(IllegalArgumentException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(CannotDeleteStudyGroupException.class)
    public ResponseEntity<Map<String, String>> handleCannotDeleteStudyGroup(CannotDeleteStudyGroupException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        if (isImportRequest(request)) {
            String errorMsg = "Validation failed: " + errors.toString();
            importHistoryService.logFailure(DUMMY_USER, errorMsg);
        }

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            errors.put(path, violation.getMessage());
        }
        if (isImportRequest(request)) {
            String errorMsg = "Constraint violation: " + errors.toString();
            importHistoryService.logFailure(DUMMY_USER, errorMsg);
        }

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler({InvalidFormatException.class})
    public ResponseEntity<Map<String, String>> handleInvalidEnum(InvalidFormatException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        String fieldName = "unknown";

        if (!ex.getPath().isEmpty()) {
            fieldName = ex.getPath().get(0).getFieldName();
        }
        String invalidValue = ex.getValue().toString();
        String targetType = ex.getTargetType().getSimpleName();
        errors.put(fieldName, "Недопустимое значение '" + invalidValue + "' для поля типа " + targetType);

        if (isImportRequest(request)) {
            importHistoryService.logFailure(DUMMY_USER, errors.toString());
        }

        return ResponseEntity.badRequest().body(errors);
    }

    private boolean isImportRequest(HttpServletRequest request) {
        if (request == null || request.getRequestURI() == null) {
            return false;
        }
        return request.getRequestURI().endsWith("/api/studygroups/import");
    }

    @ExceptionHandler(DuplicateEntityException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEntity(DuplicateEntityException ex, HttpServletRequest request) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());

        if (isImportRequest(request)) {
            importHistoryService.logFailure(DUMMY_USER, "Duplicate error: " + ex.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler({
            ConcurrencyFailureException.class,
            CannotAcquireLockException.class,
            TransactionSystemException.class
    })
    public ResponseEntity<?> handleConcurrencyFailure(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of(
                        "error", "Конфликт транзакций (попробуйте снова)"
                ));
    }
}