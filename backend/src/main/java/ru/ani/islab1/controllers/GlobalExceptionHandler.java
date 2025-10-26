package ru.ani.islab1.controllers;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.ani.islab1.exceptions.CannotDeleteStudyGroupException;
import ru.ani.islab1.exceptions.ResourceNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(ResourceNotFoundException ex) {
        Map<String,String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            String path = violation.getPropertyPath().toString();
            errors.put(path, violation.getMessage());
        }
        return ResponseEntity.badRequest().body(errors);
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

    @ExceptionHandler({InvalidFormatException.class})
    public ResponseEntity<Map<String, String>> handleInvalidEnum(InvalidFormatException ex) {
        Map<String, String> errors = new HashMap<>();

        if (!ex.getPath().isEmpty()) {
            String fieldName = ex.getPath().get(0).getFieldName();
            String invalidValue = ex.getValue().toString();
            String targetType = ex.getTargetType().getSimpleName();
            errors.put(fieldName, "Недопустимое значение '" + invalidValue + "' для поля типа " + targetType);
        } else {
            errors.put("error", "Некорректное значение");
        }

        return ResponseEntity.badRequest().body(errors);
    }
}
