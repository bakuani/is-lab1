package ru.ani.islab1.exceptions;

public class CannotDeleteStudyGroupException extends RuntimeException {
    public CannotDeleteStudyGroupException(String message) {
        super(message);
    }
}
