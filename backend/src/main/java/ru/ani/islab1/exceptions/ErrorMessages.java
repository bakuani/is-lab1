package ru.ani.islab1.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Locale;

@ToString
@RequiredArgsConstructor
public enum ErrorMessages {
    STUDYGROUP_NOT_FOUND("StudyGroup с таким id не найден: %s"),
    PERSON_NOT_FOUND("Person с таким id не найден: %s"),
    LOCATION_NOT_FOUND("Location не найден с id %s"),
    COORDINATES_MISSING("Coordinates должны содержать оба x и y"),
    LOCATION_INVALID("Location должен содержать x, y и name"),
    PERSON_ID_NULL("Person id не может быть null"),
    ID_NULL("id не может быть null"),
    GROUP_ADMIN_USED("Невозможно удалить: Админ привязан к другой группе"),
    COORDINATES_IN_USE("Невозможно удалить: такие координаты используются другой группой"),
    LOCATION_IN_USE("Невозможно удалить: такая локация используется другим админом");

    private final String template;

    public String format(Object... args) {
        return args == null || args.length == 0
                ? template
                : String.format(Locale.ROOT, template, args);
    }
}
